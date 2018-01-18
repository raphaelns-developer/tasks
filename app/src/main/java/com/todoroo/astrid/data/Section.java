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
public class Section implements Parcelable {

    // --- properties

    /** ID */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public Long id;

    /** The color parent */
    @ColumnInfo(name = "parentID")
    public Long parentID;

    /** Name of Section */
    @ColumnInfo(name = "name")
    public String name = "";

    /** Color of Section */
    @ColumnInfo(name = "color")
    public Integer color = -1;

    /** Unixtime Section was created */
    @ColumnInfo(name = "created")
    public Long created;

    /** Unixtime Section was last touched */
    @ColumnInfo(name = "modified")
    public Long modified;

    /** Unixtime Section was deleted. 0 means not deleted */
    @ColumnInfo(name = "deleted")
    public Long deleted = 0L;

    // --- data access boilerplate

    public Section() { }

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

    public void writeToXml(XmlWriter writer) {
        writer.writeLong("id", id);
        writer.writeString("name", name);
        writer.writeInteger("color", color);
        writer.writeLong("deleted", deleted);
    }


    public long getId() { return this.id; }

    public void setId(long id) {
        this.id = id;
    }

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

    public Long getDeleted() {
        return deleted;
    }

    public void setDeleted(long deleted) {
        this.deleted = deleted;
    }

    // --- parcelable helpers

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
}
