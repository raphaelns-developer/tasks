package com.todoroo.astrid.tags;

import com.todoroo.andlib.data.Property.StringProperty;
import com.todoroo.astrid.data.Metadata;

public class CaldavMetadata {

    /** Metadata key for tag data */
    public static final String KEY = "caldav"; //$NON-NLS-1$

    /** Property for reading tag values */
    public static final StringProperty CALDAV_NAME = Metadata.VALUE1;

    /** Tag uuid */
    public static final StringProperty CALDAV_UUID = new StringProperty(
            Metadata.TABLE, Metadata.VALUE2.name);

    /** Task uuid */
    public static final StringProperty TASK_UUID = new StringProperty(
            Metadata.TABLE, Metadata.VALUE3.name);


    // Creation date and deletion date are already included as part of the normal metadata entity

    /**
     * New metadata object for linking a task to the specified tag. The task
     * object should be saved and have the uuid property. All parameters
     * are manditory
     */
    public static Metadata newCaldavMetadata(long taskId, String taskUuid, String caldavName, String caldavUuid) {
        Metadata link = new Metadata();
        link.setKey(KEY);
        link.setTask(taskId);
        link.setValue(CALDAV_NAME, caldavName);
        link.setValue(TASK_UUID, taskUuid);
        link.setValue(CALDAV_UUID, caldavUuid);
        link.setDeletionDate(0L);
        return link;
    }
}
