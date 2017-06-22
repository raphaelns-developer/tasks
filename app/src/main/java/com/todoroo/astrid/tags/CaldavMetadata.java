package com.todoroo.astrid.tags;

import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.Property.StringProperty;
import com.todoroo.astrid.data.Metadata;

import java.util.UUID;

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

    public static final Property.LongProperty LAST_SYNC = new Property.LongProperty(Metadata.TABLE,
            Metadata.VALUE7.name);

    // Creation date and deletion date are already included as part of the normal metadata entity

    /**
     * New metadata object for linking a task to the specified tag. The task
     * object should be saved and have the uuid property. All parameters
     * are manditory
     */
    public static Metadata newCaldavMetadata() {
        Metadata link = new Metadata();
        link.setKey(KEY);
        return link;
    }
}
