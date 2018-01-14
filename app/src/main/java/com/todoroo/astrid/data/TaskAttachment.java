/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.data;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.Table;

@Entity(tableName = "task_attachments")
public final class TaskAttachment {

    @Deprecated
    public static final Table TABLE = new Table("task_attachments", null);

    @Deprecated
    public static final Property.LongProperty ID = new Property.LongProperty(
            TABLE, "_id");

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private Long id;

    @ColumnInfo(name = "remoteId")
    private String remoteId = RemoteModel.NO_UUID;

    @ColumnInfo(name = "task_id")
    private String taskId = RemoteModel.NO_UUID;

    @ColumnInfo(name = "name")
    private String name = "";

    @ColumnInfo(name = "path")
    private String path = "";

    @ColumnInfo(name = "content_type")
    private String contentType = "";

    @ColumnInfo(name = "deleted_at")
    private Long deleted = 0L;

    // -- Constants
    /** default directory for files on external storage */
    public static final String FILES_DIRECTORY_DEFAULT = "attachments"; //$NON-NLS-1$

    /** Constants for file types */
    public static final String FILE_TYPE_AUDIO = "audio/"; //$NON-NLS-1$
    public static final String FILE_TYPE_IMAGE = "image/"; //$NON-NLS-1$

    public static final String FILE_TYPE_OTHER = "application/octet-stream";  //$NON-NLS-1$

    public static TaskAttachment createNewAttachment(String taskUuid, String filePath, String fileName, String fileType) {
        TaskAttachment attachment = new TaskAttachment();
        attachment.setTaskId(taskUuid);
        attachment.setName(fileName);
        attachment.setPath(filePath);
        attachment.setContentType(fileType);
        attachment.setDeleted(0L);
        return attachment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getDeleted() {
        return deleted;
    }

    public void setDeleted(Long deleted) {
        this.deleted = deleted;
    }
}
