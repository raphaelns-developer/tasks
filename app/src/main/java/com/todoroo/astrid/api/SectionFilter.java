package com.todoroo.astrid.api;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Field;
import com.todoroo.andlib.sql.QueryTemplate;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.data.Section;
import com.todoroo.astrid.data.Task;

import org.tasks.R;

/**
 * Created by rapha on 17/01/2018.
 */

public class SectionFilter extends Filter {

    private static final int TAG = R.drawable.ic_label_24dp;

    private Long id;

    private SectionFilter() {
        super();
    }

    public SectionFilter(Section section) {
        super(section.getName(), queryTemplate(section.getId()), getValuesForNewTask());
        id = section.getId();
        tint = section.getColor();
        icon = TAG;
    }

    public Long getID() {
        return id;
    }

    private static QueryTemplate queryTemplate(Long id) {
        Criterion fullCriterion = Criterion.and(
                Task.SECTION_ID.eq(id),
                Task.PARENT_ID.isNull(),
                TaskDao.TaskCriteria.activeAndVisible());
        return new QueryTemplate().where(fullCriterion);
    }

    private static ContentValues getValuesForNewTask() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Section.ID.name, Task.SECTION_ID.name);

        return contentValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(Long.toString(id));
    }

    @Override
    protected void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        id = Long.valueOf(source.readString());
    }

    /**
     * Parcelable Creator Object
     */
    public static final Parcelable.Creator<SectionFilter> CREATOR = new Parcelable.Creator<SectionFilter>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public SectionFilter createFromParcel(Parcel source) {
            SectionFilter item = new SectionFilter();
            item.readFromParcel(source);
            return item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SectionFilter[] newArray(int size) {
            return new SectionFilter[size];
        }

    };

    @Override
    public boolean supportsSubtasks() {
        return true;
    }
}
