package com.todoroo.astrid.api;

import android.content.ContentValues;
import android.os.Parcel;

import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.QueryTemplate;
import com.todoroo.astrid.dao.MetadataDao;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.data.CaldavAccount;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.tags.CaldavMetadata;

import org.tasks.R;

public class CaldavFilter extends Filter {

    private static final int TAG = R.drawable.ic_label_24dp;

    private String uuid;

    private CaldavFilter() {
        super();
    }

    public CaldavFilter(CaldavAccount caldavAccount) {
        super(caldavAccount.getName(), queryTemplate(caldavAccount.getName()), getValuesForNewTask(caldavAccount));
        uuid = caldavAccount.getUuid();
        tint = caldavAccount.getColor();
        icon = TAG;
    }

    public String getUuid() {
        return uuid;
    }

    private static QueryTemplate queryTemplate(String caldavName) {
        Criterion fullCriterion = Criterion.and(
                MetadataDao.MetadataCriteria.withKey(CaldavMetadata.KEY),
                TaskDao.TaskCriteria.activeAndVisible(),
                CaldavMetadata.CALDAV_NAME.eq(caldavName));
        return new QueryTemplate().join(Join.left(Metadata.TABLE, Task.ID.eq(Metadata.TASK)))
                .where(fullCriterion);
    }

    private static ContentValues getValuesForNewTask(CaldavAccount caldavAccount) {
        ContentValues contentValues = new ContentValues();
        contentValues.putAll(CaldavMetadata.newCaldavMetadata().getMergedValues());
        contentValues.put(CaldavMetadata.CALDAV_NAME.name, caldavAccount.getName());
        return contentValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(uuid);
    }

    @Override
    protected void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        uuid = source.readString();
    }

    /**
     * Parcelable Creator Object
     */
    public static final Creator<CaldavFilter> CREATOR = new Creator<CaldavFilter>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public CaldavFilter createFromParcel(Parcel source) {
            CaldavFilter item = new CaldavFilter();
            item.readFromParcel(source);
            return item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CaldavFilter[] newArray(int size) {
            return new CaldavFilter[size];
        }

    };

    @Override
    public boolean supportsSubtasks() {
        return true;
    }
}
