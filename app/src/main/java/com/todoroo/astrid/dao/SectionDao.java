package com.todoroo.astrid.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.todoroo.andlib.sql.Criterion;
import com.todoroo.astrid.data.Section;
import com.todoroo.astrid.data.Task;

import java.util.List;

/**
 * Created by rapha on 16/01/2018.
 */
@Dao
public abstract class SectionDao {

    @Query("SELECT * FROM section WHERE name = :name COLLATE NOCASE LIMIT 1")
    public abstract Section getSectionByName(String name);

    @Query("SELECT * FROM section WHERE _id = :id")
    public abstract Section getSectionByID(Long id);

    // TODO: does this need to be ordered?
    @Query("SELECT * FROM section WHERE deleted = 0 ORDER BY _id ASC")
    public abstract List<Section> allSection();

    @Query("SELECT * FROM section WHERE deleted = 0 AND name IS NOT NULL ORDER BY UPPER(name) ASC")
    public abstract List<Section> sectionOrderedByName();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void persist(Section section);

    @Query("UPDATE section SET name = :name WHERE _id = :id")
    public abstract void rename(Long id, String name);

    @Query("DELETE FROM section WHERE _id = :id")
    public abstract void delete(Long id);

    @Insert
    public abstract void insert(Section tag);

    public void createNew(Section section) {
        insert(section);
    }

    public int update(Criterion where, Section template) {
        return update(where, template);
    }
}
