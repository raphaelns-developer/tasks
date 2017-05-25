package org.tasks.caldav;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.todoroo.astrid.dao.CaldavDao;
import com.todoroo.astrid.data.CaldavAccount;

import org.tasks.Broadcaster;
import org.tasks.injection.InjectingAbstractThreadedSyncAdapter;
import org.tasks.injection.SyncAdapterComponent;

import javax.inject.Inject;

import timber.log.Timber;

public class CalDAVSyncAdapter extends InjectingAbstractThreadedSyncAdapter {

    @Inject CaldavDao caldavDao;
    @Inject Broadcaster broadcaster;
    @Inject CaldavAccountManager caldavAccountManager;

    public CalDAVSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Timber.d("onPerformSync: " + account.name);
        CaldavAccount accountByName = caldavDao.getAccountByName(account.name);
        if (accountByName == null) {
            accountByName = new CaldavAccount();
            accountByName.setName(account.name);
            caldavDao.createNew(accountByName);
            broadcaster.refreshLists();
        } else if (accountByName.isDeleted()) {
            caldavAccountManager.removeAccount(account);
            return;
        }
        Timber.d("perform sync");
        // TODO: perform sync
    }

    @Override
    protected void inject(SyncAdapterComponent component) {
        component.inject(this);
    }
}
