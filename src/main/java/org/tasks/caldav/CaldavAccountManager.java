package org.tasks.caldav;

import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.todoroo.astrid.api.CaldavFilter;
import com.todoroo.astrid.dao.CaldavDao;
import com.todoroo.astrid.dao.MetadataDao;
import com.todoroo.astrid.data.CaldavAccount;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.service.TaskDeleter;
import com.todoroo.astrid.tags.CaldavMetadata;

import org.tasks.Broadcaster;
import org.tasks.data.TaskListDataProvider;
import org.tasks.injection.ApplicationScope;
import org.tasks.injection.ForApplication;
import org.tasks.preferences.PermissionChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static com.google.common.collect.Iterables.tryFind;

@ApplicationScope
public class CaldavAccountManager implements OnAccountsUpdateListener {

    public static final String ACCOUNT_TYPE = "org.tasks.caldav";

    private final Context context;
    private final PermissionChecker permissionChecker;
    private final android.accounts.AccountManager accountManager;
    private final TaskListDataProvider taskListDataProvider;
    private final TaskDeleter taskDeleter;
    private final MetadataDao metadataDao;
    private final Broadcaster broadcaster;
    private final CaldavDao caldavDao;

    @Inject
    public CaldavAccountManager(@ForApplication Context context, PermissionChecker permissionChecker,
                                CaldavDao caldavDao, TaskListDataProvider taskListDataProvider,
                                TaskDeleter taskDeleter, MetadataDao metadataDao, Broadcaster broadcaster) {
        this.context = context;
        this.permissionChecker = permissionChecker;
        this.caldavDao = caldavDao;
        this.taskListDataProvider = taskListDataProvider;
        this.taskDeleter = taskDeleter;
        this.metadataDao = metadataDao;
        this.broadcaster = broadcaster;
        accountManager = android.accounts.AccountManager.get(context);
        accountManager.addOnAccountsUpdatedListener(this, null, true);
        syncAccountList();
    }

    public Account getAccount(String name) {
        for (Account account : getAccounts()) {
            if (account.getName().equals(name)) {
                return account;
            }
        }
        return null;
    }

    public List<Account> getAccounts() {
        if (!permissionChecker.canAccessAccounts()) {
            return Collections.emptyList();
        }

        List<Account> accounts = new ArrayList<>();
        for (android.accounts.Account account : accountManager.getAccountsByType(ACCOUNT_TYPE)) {
            accounts.add(new Account(accountManager, account));
        }
        return accounts;
    }

    public boolean removeAccount(android.accounts.Account account) {
        CaldavAccount caldavAccount = caldavDao.getAccountByName(account.name);
        AccountManagerFuture<Boolean> future = accountManager.removeAccount(account, null, null);
        try {
            if (future.getResult()) {
                deleteAccount(caldavAccount);
                return true;
            }
        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
            Timber.e(e.getMessage(), e);
        }
        return false;
    }

    public boolean addAccount(CaldavAccount caldavAccount, String url, String username, String password) {
        android.accounts.Account account = new android.accounts.Account(caldavAccount.getName(), ACCOUNT_TYPE);
        Bundle userdata = new Bundle();
        userdata.putString(Account.EXTRA_USERNAME, username);
        userdata.putString(Account.EXTRA_URL, url);
        return accountManager.addAccountExplicitly(account, password, userdata);
    }

    private void createAccount(Account account) {
        CaldavAccount caldavAccount = new CaldavAccount();
        caldavAccount.setName(account.getName());
        caldavDao.createNew(caldavAccount);
    }

    void syncAccountList() {
        List<CaldavAccount> oldAccountList = caldavDao.getAllOrderedByName();
        List<Account> newAccountList = getAccounts();

        for (CaldavAccount local : oldAccountList) {
            Optional<Account> match = tryFind(newAccountList, remote -> local.getName().equals(remote.getName()));
            if (!match.isPresent()) {
                deleteAccount(local);
            }
        }

        for (Account remote : newAccountList) {
            Optional<CaldavAccount> match = tryFind(oldAccountList, locale -> remote.getName().equals(locale.getName()));
            if (!match.isPresent()) {
                createAccount(remote);
            }
        }
    }

    @Override
    public void onAccountsUpdated(android.accounts.Account[] accounts) {
        Timber.d("onAccountsUpdated(%s)", Lists.newArrayList(accounts));

        syncAccountList();
    }

    private void deleteAccount(CaldavAccount account) {
        taskListDataProvider
                .constructCursor(new CaldavFilter(account), Task.PROPERTIES)
                .forEach(task -> {
                    metadataDao.deleteWhere(MetadataDao.MetadataCriteria
                            .byTaskAndwithKey(task.getId(), CaldavMetadata.KEY));
                    taskDeleter.delete(task);
                });
        caldavDao.delete(account.getId());
        broadcaster.refreshLists();
    }
}
