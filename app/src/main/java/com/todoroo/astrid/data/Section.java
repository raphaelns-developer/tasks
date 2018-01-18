package com.todoroo.astrid.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.Table;
import com.todoroo.andlib.data.TodorooCursor;

import org.tasks.backup.XmlReader;
import org.tasks.backup.XmlWriter;


@Entity(tableName = "section",
        foreignKeys = @ForeignKey(entity = Section.class,
                parentColumns = "_id",
                childColumns = "parentID")
        )
public class Section extends RemoteModel {

    // --- properties

    public static final Table TABLE = new Table("section", Section.class);

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
    public Integer color = -1;
    public static final Property.IntegerProperty COLOR = new Property.IntegerProperty(
            TABLE, "color");

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
        defaultValues.put(COLOR.name, -1);
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

    @Ignore
    public Section(XmlReader reader) {
        reader.readLong("id", this::setId);
        reader.readString("name", this::setName);
        reader.readInteger("color", this::setColor);
        reader.readLong("deleted", this::setDeleted);
    }

    @Ignore
    private Section(Parcel parcel) {
        id = parcel.readLong();
        name = parcel.readString();
        color = parcel.readInt();
        deleted = parcel.readLong();
    }

    public void Section(XmlWriter writer) {
        writer.writeLong("id", id);
        writer.writeString("name", name);
        writer.writeInteger("color", color);
        writer.writeLong("deleted", deleted);
    }

    // --- data access methods

    @Override
    public long getId() { return this.id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    public Integer getColor() { return this.color; }

    public void setColor(Integer color) { this.color = color;}

    public Long getDeleted() {
        return deleted;
    }

    public void setDeleted(long deleted) {
        this.deleted = deleted;
    }

    public static final Creator<Section> CREATOR = new Creator<Section>() {
        @Override
        public Section createFromParcel(Parcel source) {
            return new Section(source);
        }

        @Override
        public Section[] newArray(int size) {
            return new Section[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(color);
        dest.writeLong(deleted);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Section section = (Section) o;

        if (id != null ? !id.equals(section.id) : section.id != null) return false;
        if (name != null ? !name.equals(section.name) : section.name != null) return false;
        if (color != null ? !color.equals(section.color) : section.color != null) return false;
        return deleted != null ? deleted.equals(section.deleted) : section.deleted == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (deleted != null ? deleted.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TagData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color=" + color +
                ", deleted=" + deleted +
                '}';
    }

    /** Checks whether section is deleted. Will return false if DELETION_DATE not read */
    public boolean isDeleted() {
        // assume false if we didn't load deletion date
        if(!containsValue(DELETION_DATE)) {
            return false;
        } else {
            return getValue(DELETION_DATE) > 0;
        }
    }
}
