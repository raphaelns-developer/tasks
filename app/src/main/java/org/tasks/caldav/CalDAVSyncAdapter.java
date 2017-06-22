package org.tasks.caldav;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;

import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.Query;
import com.todoroo.andlib.utility.DateUtilities;
import com.todoroo.astrid.dao.CaldavDao;
import com.todoroo.astrid.dao.MetadataDao;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.data.CaldavAccount;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.SyncFlags;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.tags.CaldavMetadata;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.Due;

import org.apache.commons.codec.Charsets;
import org.tasks.Broadcaster;
import org.tasks.injection.InjectingAbstractThreadedSyncAdapter;
import org.tasks.injection.SyncAdapterComponent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import at.bitfire.dav4android.BasicDigestAuthHandler;
import at.bitfire.dav4android.DavCalendar;
import at.bitfire.dav4android.DavResource;
import at.bitfire.dav4android.exception.DavException;
import at.bitfire.dav4android.exception.HttpException;
import at.bitfire.dav4android.property.GetCTag;
import at.bitfire.dav4android.property.GetETag;
import at.bitfire.ical4android.CalendarStorageException;
import at.bitfire.ical4android.InvalidCalendarException;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import timber.log.Timber;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.todoroo.astrid.data.Task.ID;
import static com.todoroo.astrid.data.Task.MODIFICATION_DATE;
import static com.todoroo.astrid.data.Task.URGENCY_SPECIFIC_DAY;
import static com.todoroo.astrid.data.Task.URGENCY_SPECIFIC_DAY_TIME;
import static org.tasks.time.DateTimeUtils.currentTimeMillis;

public class CalDAVSyncAdapter extends InjectingAbstractThreadedSyncAdapter {

    @Inject CaldavDao caldavDao;
    @Inject Broadcaster broadcaster;
    @Inject CaldavAccountManager caldavAccountManager;
    @Inject TaskDao taskDao;
    @Inject MetadataDao metadataDao;

    public CalDAVSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        String accountName = account.name;
        Timber.d("onPerformSync: " + accountName);
        // required for dav4android (ServiceLoader)
        Thread.currentThread().setContextClassLoader(getContext().getClassLoader());

        CaldavAccount caldavAccount = caldavDao.getAccountByName(accountName);
        if (caldavAccount == null) {
            caldavAccount = new CaldavAccount();
            caldavAccount.setName(accountName);
            caldavDao.createNew(caldavAccount);
            broadcaster.refreshLists();
        } else if (caldavAccount.isDeleted()) {
            caldavAccountManager.removeAccount(account);
            return;
        }
        org.tasks.caldav.Account localAccount = caldavAccountManager.getAccount(accountName);
        BasicDigestAuthHandler basicDigestAuthHandler = new BasicDigestAuthHandler(null, localAccount.getUsername(), localAccount.getPassword());
        OkHttpClient httpClient = new OkHttpClient().newBuilder()
                .addNetworkInterceptor(basicDigestAuthHandler)
                .authenticator(basicDigestAuthHandler)
                .cookieJar(new MemoryCookieStore())
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        URI uri = URI.create(localAccount.getUrl());
        HttpUrl httpUrl = HttpUrl.get(uri);
        DavCalendar davCalendar = new DavCalendar(httpClient, httpUrl);
        try {
            davCalendar.propfind(0, GetCTag.NAME);
            davCalendar.calendarQuery("VTODO", null, null);

            pushLocalChanges(accountName, httpClient, httpUrl);

            // fetch and apply remote changes
            for (DavResource vCard : davCalendar.members) {
                ResponseBody responseBody = vCard.get("text/calendar");
                GetETag eTag = (GetETag) vCard.properties.get(GetETag.NAME);
                if (eTag == null || isNullOrEmpty(eTag.eTag)) {
                    throw new DavException("Received CalDAV GET response without ETag for " + vCard.location);
                }
                MediaType contentType = responseBody.contentType();
                Charset charset = contentType == null ? Charsets.UTF_8 : contentType.charset(Charsets.UTF_8);
                InputStream stream = responseBody.byteStream();
                try {
                    processVTodo(vCard.fileName(), accountName, eTag.eTag, stream, charset);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
        } catch (IOException | HttpException | DavException | CalendarStorageException e) {
            Timber.e(e.getMessage(), e);
        }
    }

    private void pushLocalChanges(String accountName, OkHttpClient httpClient, HttpUrl httpUrl) {
        List<Task> tasks = taskDao.toList(Query.select(Task.PROPERTIES)
                .join(Join.left(Metadata.TABLE, Criterion.and(MetadataDao.MetadataCriteria.withKey(CaldavMetadata.KEY), ID.eq(Metadata.TASK))))
                .where(Criterion.and(MODIFICATION_DATE.gt(CaldavMetadata.LAST_SYNC), CaldavMetadata.CALDAV_NAME.eq(accountName))));
        for (com.todoroo.astrid.data.Task task : tasks) {
            try {
                pushTask(task, task.getMergedValues(), accountName, httpClient, httpUrl);
            } catch (IOException e) {
                Timber.e(e, e.getMessage());
            }
        }
    }

    private boolean deleteRemoteResource(OkHttpClient httpClient, HttpUrl httpUrl, Metadata metadata) {
        DavResource remote = new DavResource(httpClient, httpUrl.newBuilder().addPathSegment(metadata.getValue(CaldavMetadata.CALDAV_UUID) + ".ics").build());
        try {
            remote.delete(null);
            metadataDao.delete(metadata.getId());
            return true;
        } catch (IOException | HttpException e) {
            Timber.e(e.getMessage(), e);
            return false;
        }
    }

    private void pushTask(Task task, ContentValues values, String accountName, OkHttpClient httpClient, HttpUrl httpUrl) throws IOException {
        Timber.d("pushing %s", task);
        List<Metadata> deleted = getDeleted(task.getId(), accountName);
        if (!deleted.isEmpty()) {
            for (Metadata entry : deleted) {
                deleteRemoteResource(httpClient, httpUrl, entry);
            }
            return;
        }

        Metadata caldavMetadata = metadataDao.getFirst(Query.select(Metadata.PROPERTIES).where(Criterion.and(
                MetadataDao.MetadataCriteria.byTaskAndwithKey(task.getId(), CaldavMetadata.KEY),
                CaldavMetadata.CALDAV_NAME.eq(accountName),
                MetadataDao.MetadataCriteria.isActive())));

        at.bitfire.ical4android.Task remoteModel;
        boolean newlyCreated = false;

        if (caldavMetadata == null || !caldavMetadata.containsNonNullValue(CaldavMetadata.CALDAV_UUID) ||
                TextUtils.isEmpty(caldavMetadata.getValue(CaldavMetadata.CALDAV_UUID))) {
            String caldavUid = UUID.randomUUID().toString();
            if (caldavMetadata == null) {
                caldavMetadata = CaldavMetadata.newCaldavMetadata();
                caldavMetadata.setTask(task.getId());
                caldavMetadata.setValue(CaldavMetadata.CALDAV_NAME, accountName);
            }
            caldavMetadata.setValue(CaldavMetadata.CALDAV_UUID, caldavUid);

            remoteModel = new at.bitfire.ical4android.Task();
            remoteModel.uid = caldavUid;
            newlyCreated = true;
        } else {
            remoteModel = new at.bitfire.ical4android.Task();
            remoteModel.uid = caldavMetadata.getValue(CaldavMetadata.CALDAV_UUID);
        }

        //If task was newly created but without a title, don't sync--we're in the middle of
        //creating a task which may end up being cancelled. Also don't sync new but already
        //deleted tasks
        if (newlyCreated &&
                (!values.containsKey(Task.TITLE.name) || TextUtils.isEmpty(task.getTitle()) || task.getDeletionDate() > 0)) {
            return;
        }

        if (task.isDeleted()) {
            if (deleteRemoteResource(httpClient, httpUrl, caldavMetadata)) {
                taskDao.delete(task.getId());
            }
        } else {
            remoteModel.createdAt = task.getCreationDate();
//            remoteModel.lastModified = task.getModificationDate();
            remoteModel.summary = task.getTitle();
            if (values.containsKey(Task.NOTES.name)) {
                remoteModel.description = task.getNotes();
            }
            if (task.hasDueDate()) {
                remoteModel.due = new Due(task.hasDueTime()
                        ? new DateTime(task.getDueDate())
                        : new Date(task.getDueDate()));
            }
            if (task.isCompleted()) {
                remoteModel.completedAt = new Completed(new DateTime(task.getCompletionDate()));
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            remoteModel.write(os);
            RequestBody requestBody = RequestBody.create(
                    DavCalendar.MIME_ICALENDAR,
                    os.toByteArray());
            try {
                DavResource remote = new DavResource(httpClient, httpUrl.newBuilder().addPathSegment(caldavMetadata.getValue(CaldavMetadata.CALDAV_UUID) + ".ics").build());
                remote.put(requestBody, null, false);
            } catch (HttpException e) {
                Timber.e(e.getMessage(), e);
                return;
            }

            task.setModificationDate(DateUtilities.now());
            caldavMetadata.setValue(CaldavMetadata.LAST_SYNC, DateUtilities.now() + 1000L);
            metadataDao.persist(caldavMetadata);
            task.putTransitory(SyncFlags.GTASKS_SUPPRESS_SYNC, true);
            taskDao.saveExistingWithSqlConstraintCheck(task);
        }
    }

    private List<Metadata> getDeleted(long taskId, String accountName) {
        return metadataDao.toList(Criterion.and(
                MetadataDao.MetadataCriteria.byTaskAndwithKey(taskId, CaldavMetadata.KEY),
                CaldavMetadata.CALDAV_NAME.eq(accountName),
                MetadataDao.MetadataCriteria.isDeleted()));
    }

    private void processVTodo(String fileName, String accountName, String eTag, InputStream stream, Charset charset) throws IOException, CalendarStorageException {
        at.bitfire.ical4android.Task[] tasks;
        try {
            tasks = at.bitfire.ical4android.Task.fromStream(stream, charset);
        } catch (InvalidCalendarException e) {
            Timber.e(e.getMessage(), e);
            return;
        }

        if (tasks.length == 1) {
            at.bitfire.ical4android.Task remote = tasks[0];
            Task task;
            Metadata caldavMetadata = metadataDao.getFirst(Query.select(Metadata.PROPERTIES).where(Criterion.and(
                    Metadata.KEY.eq(CaldavMetadata.KEY),
                    CaldavMetadata.CALDAV_NAME.eq(accountName),
                    MetadataDao.MetadataCriteria.isActive(),
                    CaldavMetadata.CALDAV_UUID.eq(remote.uid))));
            if (caldavMetadata == null) {
                task = new Task();
                taskDao.save(task);
                caldavMetadata = CaldavMetadata.newCaldavMetadata();
                caldavMetadata.setTask(task.getId());
                caldavMetadata.setValue(CaldavMetadata.TASK_UUID, task.getUuid());
                caldavMetadata.setValue(CaldavMetadata.CALDAV_UUID, remote.uid);
                caldavMetadata.setValue(CaldavMetadata.CALDAV_NAME, accountName);
                Timber.d("NEW %s", remote);
            } else {
                task = taskDao.fetch(caldavMetadata.getTask(), Task.PROPERTIES);
                Timber.d("UPDATE %s", remote);
            }

            task.setCreationDate(remote.createdAt);
            if (remote.completedAt != null) {
                task.setCompletionDate(remote.completedAt.getDateTime().getTime());
            }
            task.setTitle(remote.summary);
            task.setNotes(remote.description);
            if (remote.due != null) {
                Date due = remote.due.getDate();
                if (due instanceof DateTime) {
                    task.setDueDate(Task.createDueDate(URGENCY_SPECIFIC_DAY_TIME, due.getTime()));
                } else {
                    task.setDueDate(Task.createDueDate(URGENCY_SPECIFIC_DAY, due.getTime()));
                }
            }
            task.setModificationDate(currentTimeMillis());
            caldavMetadata.setValue(CaldavMetadata.LAST_SYNC, DateUtilities.now() + 1000L);
            metadataDao.persist(caldavMetadata);
            task.putTransitory(SyncFlags.GTASKS_SUPPRESS_SYNC, true);
            taskDao.saveExistingWithSqlConstraintCheck(task);
        } else {
            Timber.e("Received VCALENDAR with %s VTODOs; ignoring %s", tasks.length, fileName);
        }
    }

    @Override
    protected void inject(SyncAdapterComponent component) {
        component.inject(this);
    }
}
