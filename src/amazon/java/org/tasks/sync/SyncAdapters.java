package org.tasks.sync;

import android.content.ContentResolver;

import com.todoroo.astrid.activity.TaskListFragment;

import org.tasks.caldav.CaldavSyncAdapterHelper;

import javax.inject.Inject;

public class SyncAdapters {
    private CaldavSyncAdapterHelper caldavSyncAdapterHelper;

    @Inject
    public SyncAdapters(CaldavSyncAdapterHelper caldavSyncAdapterHelper) {
        this.caldavSyncAdapterHelper = caldavSyncAdapterHelper;
    }

    public boolean initiateManualSync() {
        return caldavSyncAdapterHelper.initiateManualSync();
    }

    public void requestSynchronization() {
        caldavSyncAdapterHelper.requestSynchronization();
    }

    public boolean isGoogleTaskEnabled() {
        return false;
    }

    public void checkPlayServices(TaskListFragment taskListFragment) {

    }

    public boolean isMasterSyncEnabled() {
        return ContentResolver.getMasterSyncAutomatically();
    }
}
