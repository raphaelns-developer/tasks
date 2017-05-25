package org.tasks.caldav;

import android.content.ContentResolver;
import android.os.Bundle;

import javax.inject.Inject;

public class CaldavSyncAdapterHelper {

    private static final String AUTHORITY = "org.tasks";

    private final CaldavAccountManager accountManager;

    @Inject
    public CaldavSyncAdapterHelper(CaldavAccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public boolean initiateManualSync() {
        for (org.tasks.caldav.Account account : accountManager.getAccounts()) {
            Bundle extras = new Bundle();
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            ContentResolver.requestSync(account.getAccount(), AUTHORITY, extras);
        }
        return true;
    }


    public void requestSynchronization() {
        for (org.tasks.caldav.Account account : accountManager.getAccounts()) {
            ContentResolver.requestSync(account.getAccount(), AUTHORITY, new Bundle());
        }
    }
}
