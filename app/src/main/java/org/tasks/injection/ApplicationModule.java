package org.tasks.injection;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.todoroo.astrid.dao.Database;
import com.todoroo.astrid.dao.SectionDao;
import com.todoroo.astrid.dao.StoreObjectDao;
import com.todoroo.astrid.dao.TagDataDao;
import com.todoroo.astrid.dao.TaskAttachmentDao;
import com.todoroo.astrid.dao.TaskListMetadataDao;
import com.todoroo.astrid.dao.UserActivityDao;
import com.todoroo.astrid.provider.Astrid2TaskProvider;

import org.tasks.ErrorReportingSingleThreadExecutor;
import org.tasks.analytics.Tracker;
import org.tasks.data.AlarmDao;
import org.tasks.data.LocationDao;
import org.tasks.db.Migrations;
import org.tasks.locale.Locale;
import org.tasks.notifications.NotificationDao;

import java.util.concurrent.Executor;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    private final Context context;

    public ApplicationModule(Context context) {
        this.context = context;
    }

    @Provides
    public Locale getLocale() {
        return Locale.getInstance(context);
    }

    @Provides
    @ForApplication
    public Context getApplicationContext() {
        return context;
    }

    @Provides
    @ApplicationScope
    @Named("iab-executor")
    public Executor getIabExecutor(Tracker tracker) {
        return new ErrorReportingSingleThreadExecutor("iab-executor", tracker);
    }

    @Provides
    @ApplicationScope
    public Database getAppDatabase() {
        return Room
                .databaseBuilder(context, Database.class, Database.NAME)
                .allowMainThreadQueries() // TODO: remove me
                .addMigrations(Migrations.MIGRATIONS)
                .build()
                .setOnDatabaseUpdated(() -> Astrid2TaskProvider.notifyDatabaseModification(context));
    }

    @Provides
    public NotificationDao getNotificationDao(Database database) {
        return database.notificationDao();
    }

    @Provides
    public TagDataDao getTagDataDao(Database database) {
        return database.getTagDataDao();
    }

    @Provides
    public SectionDao getSectionDao(Database database) {
        return database.getSectionDao();
    }

    @Provides
    public UserActivityDao getUserActivityDao(Database database) {
        return database.getUserActivityDao();
    }

    @Provides
    public TaskAttachmentDao getTaskAttachmentDao(Database database) {
        return database.getTaskAttachmentDao();
    }

    @Provides
    public TaskListMetadataDao getTaskListMetadataDao(Database database) {
        return database.getTaskListMetadataDao();
    }

    @Provides
    public StoreObjectDao getStoreObjectDao(Database database) {
        return database.getStoreObjectDao();
    }

    @Provides
    public AlarmDao getAlarmDao(Database database) {
        return database.getAlarmDao();
    }

    @Provides
    public LocationDao getGeofenceDao(Database database) {
        return database.getGeofenceDao();
    }
}
