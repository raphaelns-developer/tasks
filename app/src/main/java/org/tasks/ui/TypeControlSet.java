package org.tasks.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.todoroo.astrid.data.Task;

import org.tasks.R;
import org.tasks.injection.ForActivity;
import org.tasks.injection.FragmentComponent;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TypeControlSet extends TaskEditControlFragment {
    public static final int TAG = R.string.TEA_ctrl_type_pref;
    private static final String EXTRA_TYPE = "extra_type";

    @Inject
    @ForActivity
    Context context;

    @BindView(R.id.taskType)
    ImageButton button;
    private AlertDialog dialog;

    private Integer type;
    private Boolean isNewTask;

    @Override
    protected void inject(FragmentComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), null);
        ButterKnife.bind(this, view);

        if (savedInstanceState != null) {
            type = savedInstanceState.getInt(EXTRA_TYPE);
        }

        if(isNewTask) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setSingleChoiceItems(getResources().getStringArray(R.array.TEA_type), type,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            type = which;
                            updateButton();
                            dialog.dismiss();
                        }
                    });
            dialog = builder.create();
        }
        updateButton();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_TYPE, type);
    }

    @OnClick(R.id.taskType)
    void onClick(View view) {
        if(isNewTask) {
            dialog.show();
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.control_set_type;
    }

    @Override
    protected int getIcon() {
        return -1;
    }

    @Override
    public int controlId() {
        return TAG;
    }

    @Override
    public void initialize(boolean isNewTask, Task task) {
        this.type = task.getType();
        this.isNewTask = isNewTask;
    }

    @Override
    public boolean hasChanges(Task original) {
        return type != original.getType();
    }

    @Override
    public void apply(Task task) {
        task.setType(this.type);
    }

    private void updateButton() {
        button.setImageResource(type == 0 ? R.drawable.ic_type_task_24dp : R.drawable.ic_type_project_24dp);
    }
}
