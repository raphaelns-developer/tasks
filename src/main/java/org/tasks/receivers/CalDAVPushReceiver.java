package org.tasks.receivers;

import android.content.Context;
import android.content.Intent;

import com.todoroo.astrid.api.AstridApiConstants;
import com.todoroo.astrid.data.SyncFlags;
import com.todoroo.astrid.data.Task;

import org.tasks.caldav.CaldavAccountManager;
import org.tasks.injection.BroadcastComponent;
import org.tasks.injection.InjectingBroadcastReceiver;

import javax.inject.Inject;

public class CalDAVPushReceiver extends InjectingBroadcastReceiver {

    @Inject CaldavAccountManager caldavAccountManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Task model = intent.getParcelableExtra(AstridApiConstants.EXTRAS_TASK);
        if (model == null) {
            return;
        }
        if(model.checkTransitory(SyncFlags.GTASKS_SUPPRESS_SYNC)) {
            return;
        }
        caldavAccountManager.requestSynchronization();
    }

    @Override
    protected void inject(BroadcastComponent component) {
        component.inject(this);
    }
}
