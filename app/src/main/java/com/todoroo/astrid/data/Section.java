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
    public static final Table TABLE = new Table("sections", Section.class);

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

    /** List of all properties for this model */
    public static final Property<?>[] PROPERTIES = generateProperties(Section.class);

    // --- defaults

    /** Default values container */
    private static final ContentValues defaultValues = new ContentValues();

    static {
        defaultValues.put(NAME.name, "");
        defaultValues.put(COLOR.name, 0L);
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
        return getIdHelper(ID);
    }

    // --- parcelable helpers

    public static final Creator<Task> CREATOR = new ModelCreator<>(Task.class);

    // --- data access methods

    public String getName() { return getValue(NAME); }

    public void setValue(String name) { setValue(NAME, name);}

    public Integer geColor() { return getValue(COLOR); }

    public void setColor(Integer color) { setValue(COLOR, color);}
}
