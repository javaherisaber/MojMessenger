package ir.logicbase.mojmessenger.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import ir.logicbase.mojmessenger.database.contact.ContactDao;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.database.message.MessageDao;
import ir.logicbase.mojmessenger.database.message.MessageEntity;

/**
 * The Room database
 */
@Database(entities = {ContactEntity.class, MessageEntity.class}, version = 2)
abstract class MojDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "MojMessenger.db";

    private static final Object sLock = new Object();
    private static MojDatabase INSTANCE;

    public abstract ContactDao contactDao();

    public abstract MessageDao messageDao();

    /**
     * Migrate from:
     * version 1 - using the SQLiteDatabase API
     * to
     * version 2 - using Room
     */
    @VisibleForTesting
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Room uses an own database hash to uniquely identify the database
            // Since version 1 does not use Room, it doesn't have the database hash associated.
            // By implementing a Migration class, we're telling Room that it should use the data
            // from version 1 to version 2.
            // If no migration is provided, then the tables will be dropped and recreated.
            // Since we didn't alter the table, there's nothing else to do here.
        }
    };

    public static MojDatabase getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        MojDatabase.class, DATABASE_NAME)
                        .addMigrations(MIGRATION_1_2)
                        .build();
            }
            return INSTANCE;
        }
    }

}
