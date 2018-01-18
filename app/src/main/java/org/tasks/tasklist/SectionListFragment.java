package org.tasks.tasklist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.todoroo.astrid.activity.TaskListActivity;
import com.todoroo.astrid.activity.TaskListFragment;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.api.SectionFilter;
import com.todoroo.astrid.data.Section;

import org.tasks.R;
import org.tasks.activities.SectionSettingsActivity;
import org.tasks.injection.FragmentComponent;

/**
 * Created by rapha on 18/01/2018.
 */

public class SectionListFragment extends TaskListFragment {

    private static final int REQUEST_EDIT_SECTION = 11544;

    public static TaskListFragment newSectionListFragment(SectionFilter filter, Section section) {
        SectionListFragment fragment = new SectionListFragment();
        fragment.filter = filter;
        fragment.section = section;
        return fragment;
    }

    private static final String EXTRA_SECTION_DATA = "extra_section_data";

    protected Section section;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            section = savedInstanceState.getParcelable(EXTRA_SECTION_DATA);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void inflateMenu(Toolbar toolbar) {
        super.inflateMenu(toolbar);
        toolbar.inflateMenu(R.menu.menu_section_view_fragment);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_section_settings:
                Intent intent = new Intent(getActivity(), SectionSettingsActivity.class);
                intent.putExtra(SectionSettingsActivity.EXTRA_SECTION_DATA, section);
                startActivityForResult(intent, REQUEST_EDIT_SECTION);
                return true;
            default:
                return super.onMenuItemClick(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_SECTION) {
            if (resultCode == Activity.RESULT_OK) {
                String action = data.getAction();
                TaskListActivity activity = (TaskListActivity) getActivity();
                if (SectionSettingsActivity.ACTION_DELETED.equals(action)) {
                    activity.onFilterItemClicked(null);
                } else if (SectionSettingsActivity.ACTION_RELOAD.equals(action)) {
                    activity.getIntent().putExtra(TaskListActivity.OPEN_FILTER,
                            (Filter) data.getParcelableExtra(TaskListActivity.OPEN_FILTER));
                    activity.recreate();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(EXTRA_SECTION_DATA, section);
    }

    @Override
    protected boolean hasDraggableOption() {
        return section != null;
    }

    @Override
    public void inject(FragmentComponent component) {
        component.inject(this);
    }
}
