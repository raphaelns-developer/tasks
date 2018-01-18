package com.todoroo.astrid.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.ContentValues;

import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.Table;
import com.todoroo.andlib.data.TodorooCursor;


@Entity(tableName = "section",
        foreignKeys = @ForeignKey(entity = Section.class,
                parentColumns = "_id",
                childColumns = "parentID")
        )
public class Section extends RemoteModel {
    // --- table and uri

    /** table for this model */
    public static final Table TABLE = new Table("section", Section.class);

    // --- properties

    /** ID */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public Long id;
    public static final Property.LongProperty ID = new Property.LongProperty(
            TABLE, ID_PROPERTY_NAME);

    /** The color parent */
    @ColumnInfo(name = "parentID")
    public Long parentID;
    public static final Property.LongProperty PARENT_ID = new Property.LongProperty(
            TABLE, "parentID");

    /** Name of Section */
    @ColumnInfo(name = "name")
    public String name = "";
    public static final Property.StringProperty NAME = new Property.StringProperty(
            TABLE, "name");

    /** Color of Section */
    @ColumnInfo(name = "color")
    public Integer color = 0;
    public static final Property.IntegerProperty COLOR = new Property.IntegerProperty(
            TABLE, "color");

    /** Unixtime Section was created */
    @ColumnInfo(name = "created")
    public Long created;
    public static final Property.LongProperty CREATION_DATE = new Property.LongProperty(
            TABLE, "created", Property.PROP_FLAG_DATE);

    /** Unixtime Section was last touched */
    @ColumnInfo(name = "modified")
    public Long modified;
    public static final Property.LongProperty MODIFICATION_DATE = new Property.LongProperty(
            TABLE, "modified", Property.PROP_FLAG_DATE);

    /** Unixtime Section was deleted. 0 means not deleted */
    @ColumnInfo(name = "deleted")
    public Long deleted = 0L;
    public static final Property.LongProperty DELETION_DATE = new Property.LongProperty(
            TABLE, "deleted", Property.PROP_FLAG_DATE);

    /** List of all properties for this model */
    public static final Property<?>[] PROPERTIES = generateProperties(Section.class);

    // --- defaults

    /** Default values container */
    private static final ContentValues defaultValues = new ContentValues();

    static {
        defaultValues.put(NAME.name, "");
        defaultValues.put(COLOR.name, 0);
    }

    @Override
    public ContentValues getDefaultValues() {
        return defaultValues;
    }

    // --- data access boilerplate

    public Section() {
        super();
    }

    @Ignore
    public Section(TodorooCursor<Section> cursor) {
        super(cursor);
    }

    @Ignore
    public Section(Section section) {
        super(section);
    }

    @Override
    public long getId() {
        return this.id;
    }

    // --- parcelable helpers

    public static final Creator<Task> CREATOR = new ModelCreator<>(Task.class);

    // --- data access methods

    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    public Integer getColor() { return this.color; }

    public void setColor(Integer color) { this.color = color;}

    public Long getCreationDate() {
        return this.created;
    }

    public void setCreationDate(Long creationDate) {
        this.created = creationDate;
    }

    public void setModificationDate(Long modificationDate) {
        this.modified = modificationDate;
    }

    public Long getDeletionDate() {
        return this.deleted;
    }

    public void setDeletionDate(Long deletionDate) {
        this.deleted = deletionDate;
    }


    /** Checks whether task is deleted. Will return false if DELETION_DATE not read */
    public boolean isDeleted() {
        // assume false if we didn't load deletion date
        if(!containsValue(DELETION_DATE)) {
            return false;
        } else {
            return getValue(DELETION_DATE) > 0;
        }
    }
}
