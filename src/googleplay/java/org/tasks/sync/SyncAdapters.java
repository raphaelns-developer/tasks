package org.tasks.sync;

import android.content.ContentResolver;

import com.todoroo.astrid.activity.TaskListFragment;

import org.tasks.caldav.CaldavSyncAdapterHelper;
import org.tasks.gtasks.GtaskSyncAdapterHelper;

import javax.inject.Inject;

public class SyncAdapters {

    private final GtaskSyncAdapterHelper gtaskSyncAdapterHelper;
    private final CaldavSyncAdapterHelper caldavSyncAdapterHelper;

    @Inject
    public SyncAdapters(GtaskSyncAdapterHelper gtaskSyncAdapterHelper, CaldavSyncAdapterHelper caldavSyncAdapterHelper) {
        this.gtaskSyncAdapterHelper = gtaskSyncAdapterHelper;
        this.caldavSyncAdapterHelper = caldavSyncAdapterHelper;
    }

    public void requestSynchronization() {
        gtaskSyncAdapterHelper.requestSynchronization();
        caldavSyncAdapterHelper.requestSynchronization();
    }

    public boolean initiateManualSync() {
        return gtaskSyncAdapterHelper.initiateManualSync() | caldavSyncAdapterHelper.initiateManualSync();
    }

    public boolean isMasterSyncEnabled() {
        return ContentResolver.getMasterSyncAutomatically();
    }

    public boolean isGoogleTaskEnabled() {
        return gtaskSyncAdapterHelper.isEnabled();
    }

    public void checkPlayServices(TaskListFragment taskListFragment) {
        gtaskSyncAdapterHelper.checkPlayServices(taskListFragment);
    }
}
