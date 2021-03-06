package com.chatapp.ramji.buddyplans.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import com.chatapp.ramji.buddyplans.R;

/**
 * Created by ramji_v on 10/7/2017.
 */
@Database(entities = {MessageEntity.class, SavedChatsEntity.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract MessageDAO messageModel();

    public abstract SavedChatsEntityDAO savedchatsModel();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, context.getString(R.string.db_name))
//                         Room.inMemoryDatabaseBuilder(context.getApplicationContext(), AppDatabase.class)
                            // To simplify the codelab, allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .addMigrations(MIGRATION_1_2)
                            .build();
        }
        return INSTANCE;
    }

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE SavedChatsEntity "
                    + " ADD COLUMN active INTEGER NOT NULL default 1");
        }
    };

    public static void destroyInstance() {
        INSTANCE = null;
    }


}
