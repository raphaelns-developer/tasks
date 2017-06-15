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

import okhttp3.OkHttpClient;
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
        CaldavAccount caldavAccount = caldavDao.getAccountByName(account.name);
        if (caldavAccount == null) {
            caldavAccount = new CaldavAccount();
            caldavAccount.setName(account.name);
            caldavDao.createNew(caldavAccount);
            broadcaster.refreshLists();
        } else if (caldavAccount.isDeleted()) {
            caldavAccountManager.removeAccount(account);
            return;
        }
        org.tasks.caldav.Account localAccount = caldavAccountManager.getAccount(account.name);
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.addInterceptor(new BasicAuthInterceptor(localAccount.getUsername(), localAccount.getPassword()));

        Timber.d("perform sync");
        // TODO: perform sync
    }

    @Override
    protected void inject(SyncAdapterComponent component) {
        component.inject(this);
    }
}
