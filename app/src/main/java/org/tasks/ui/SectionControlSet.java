package org.tasks.ui;

import android.support.v7.app.AlertDialog;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Ordering;
import com.todoroo.andlib.utility.DateUtilities;
import com.todoroo.astrid.dao.SectionDao;
import com.todoroo.astrid.data.RemoteModel;
import com.todoroo.astrid.data.Section;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.utility.Flags;

import org.tasks.R;
import org.tasks.dialogs.DialogBuilder;
import org.tasks.injection.FragmentComponent;
import org.tasks.themes.Theme;
import org.tasks.themes.ThemeCache;
import org.tasks.themes.ThemeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Lists.transform;

/**
 * Created by rapha on 18/01/2018.
 */

public class SectionControlSet extends TaskEditControlFragment {
    public static final int TAG = R.string.TEA_ctrl_section_pref;

    private static final char SPACE = '\u0020';
    private static final char NO_BREAK_SPACE = '\u00a0';
    private static final String EXTRA_SELECTED_SECTION = "extra_selected_section";

    @Inject SectionDao sectionDao;
    @Inject DialogBuilder dialogBuilder;
    @Inject ThemeCache themeCache;
    @Inject Theme theme;

    @BindView(R.id.display_row_edit) TextView tagsDisplay;

    private long taskId;
    private ListView sectionListView;
    private View dialogView;
    private AlertDialog dialog;
    private List<Section> allSection;
    private Long sectionID;
    private SingleCheckedArrayAdapter adapter;


    private final Ordering<Section> orderByName = new Ordering<Section>() {
        @Override
        public int compare(Section left, Section right) {
            return left.getName().compareTo(right.getName());
        }
    };

    private SpannableString sectionToString(Section section, final float maxLength) {
        String sectionName = section.getName();
        sectionName = sectionName
                .substring(0, Math.min(sectionName.length(), (int) maxLength))
                .replace(' ', NO_BREAK_SPACE);
        SpannableString string = new SpannableString(NO_BREAK_SPACE + sectionName + NO_BREAK_SPACE);
        int themeIndex = section.getColor();
        ThemeColor color = themeIndex >= 0 ? themeCache.getThemeColor(themeIndex) : themeCache.getUntaggedColor();
        string.setSpan(new BackgroundColorSpan(color.getPrimaryColor()), 0, string.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        string.setSpan(new ForegroundColorSpan(color.getActionBarTint()), 0, string.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return string;
    }

    private CharSequence buildSectionString() {
        SpannableString tagString = sectionToString(getSelectedSection(), Float.MAX_VALUE);
        SpannableStringBuilder builder = new SpannableStringBuilder();
         builder.append(tagString);

        return builder;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), null);
        ButterKnife.bind(this, view);

        if (savedInstanceState != null) {
            sectionID = savedInstanceState.getLong(EXTRA_SELECTED_SECTION);
        }

        allSection = sectionDao.allSection();
        dialogView = inflater.inflate(R.layout.control_set_section_list, null);
        sectionListView = dialogView.findViewById(R.id.existingSections);

        theme.applyToContext(getActivity());
        sectionListView.setAdapter(new SingleCheckedArrayAdapter(getActivity(), transform(allSection, Section::getName), theme.getThemeAccent()) {
            @Override
            protected int getDrawable(int position) {
                return R.drawable.ic_label_24dp;
            }

            @Override
            protected int getDrawableColor(int position) {
                return allSection.get(position).getColor();
            }
        });

        setSectionSelected(sectionID);

        refreshDisplayView();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(EXTRA_SELECTED_SECTION, sectionID);
    }

    @Override
    protected int getLayout() {
        return R.layout.control_set_section;
    }

    @Override
    public void initialize(boolean isNewTask, Task task) {
        sectionID = task.getSectionID();
    }

    @Override
    public void apply(Task task) { task.setSectionID(this.sectionID); }

    @OnClick(R.id.display_row_edit)
    void openPopup(View view) {
        if (dialog == null) {
            dialog = buildDialog();
        }
        dialog.show();
    }

    private AlertDialog buildDialog() {
        return dialogBuilder.newDialog()
                .setView(dialogView)
                .setOnDismissListener(dialogInterface -> refreshDisplayView())
                .create();
    }

    private void setSectionSelected(Long sectionID) {
        int index = 0;
        for (Section item : allSection) {
            if(item.getId() == sectionID)
            {
                index = allSection.indexOf(item);
                break;
            }
        }
        if (index >= 0) {
            sectionListView.setItemChecked(index, true);
        }
    }

    private Long getSelectedSectionID() {
        long selected = 0;
        for(int i = 0; i < sectionListView.getAdapter().getCount(); i++) {
            if (sectionListView.isItemChecked(i)) {
                selected = i;
                break;
            }
        }
        return selected;
    }

    private Section getSelectedSection() {
        Section selected = null;
        for(int i = 0; i < sectionListView.getAdapter().getCount(); i++) {
            if (sectionListView.isItemChecked(i)) {
                selected = allSection.get(i);
                break;
            }
        }
        return selected;
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_label_24dp;
    }

    @Override
    public int controlId() {
        return TAG;
    }

    private void refreshDisplayView() {
        sectionID = getSelectedSectionID();
        CharSequence sectionString = buildSectionString();
        if (TextUtils.isEmpty(sectionString)) {
            tagsDisplay.setText(R.string.tag_FEx_untagged);
        } else {
            tagsDisplay.setText(sectionString);
        }
    }

    @Override
    protected void inject(FragmentComponent component) {
        component.inject(this);
    }
}
