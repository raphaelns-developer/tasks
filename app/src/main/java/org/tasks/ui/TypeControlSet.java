package org.tasks.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.widget.CompoundButton;

import com.todoroo.astrid.data.Task;

import org.tasks.R;

import butterknife.OnClick;

/**
 * Created by rapha on 13/01/2018.
 */

public class TypeControlSet extends AppCompatButton {
    public static final int TAG = R.string.TEA_ctrl_type_pref;

    private int type;

    public TypeControlSet(Context context, Task task) {
        super(context);

        updateIcon(task.getType());
    }

    private void updateIcon(int type) {
        setBackgroundResource(type == 0 ? R.drawable.ic_type_task_24dp : R.drawable.ic_type_project_24dp);
    }

    @OnClick({R.id.type_button})
    void onTypeChanged(CompoundButton button) {
        openTypeDialog();
    }

    private void openTypeDialog() {
    }
}
