package com.todoroo.astrid.subtasks;

import android.app.Activity;
import android.content.Context;

import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.astrid.activity.TaskListFragment;
import com.todoroo.astrid.adapter.TaskAdapter;
import com.todoroo.astrid.api.SectionFilter;
import com.todoroo.astrid.dao.SectionDao;
import com.todoroo.astrid.data.Section;
import com.todoroo.astrid.data.Task;

import org.tasks.injection.ForApplication;
import org.tasks.injection.FragmentComponent;
import org.tasks.tasklist.SectionListFragment;
import org.tasks.themes.Theme;

import javax.inject.Inject;

/**
 * Created by rapha on 18/01/2018.
 */

public class SubtasksSectionListFragment extends SectionListFragment {

    public static TaskListFragment newSubtasksSectionListFragment(SectionFilter filter, Section section) {
        SubtasksSectionListFragment fragment = new SubtasksSectionListFragment();
        fragment.filter = filter;
        fragment.section = section;
        return fragment;
    }

    @Inject @ForApplication Context context;
    @Inject SectionDao sectionDao;
    @Inject Theme theme;
    @Inject AstridOrderedListFragmentHelper helper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        helper.setTaskListFragment(this);
    }

    @Override
    public void setTaskAdapter() {
//        helper.setList(section);
        helper.beforeSetUpTaskList(filter);

        super.setTaskAdapter();
    }

    @Override
    public void onTaskCreated(String uuid) {
        helper.onCreateTask(uuid);
    }

    @Override
    protected void onTaskDelete(Task task) {
        super.onTaskDelete(task);
        helper.onDeleteTask(task);
    }

    @Override
    protected TaskAdapter createTaskAdapter(TodorooCursor<Task> cursor) {
        return helper.createTaskAdapter(theme.wrap(context), cursor);
    }

    @Override
    public void inject(FragmentComponent component) {
        component.inject(this);
    }
}
