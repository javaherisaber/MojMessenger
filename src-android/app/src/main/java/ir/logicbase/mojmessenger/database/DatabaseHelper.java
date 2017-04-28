package ir.logicbase.mojmessenger.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Mahdi on 8/8/2017.
 * bridge to android SQLite framework
 */

class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MojMessenger.db";
    private static final int DATABASE_VERSION = 1;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.SQL_CREATE_TABLE_CHAT);
        db.execSQL(DatabaseContract.SQL_CREATE_TABLE_MESSAGE);
        db.execSQL(DatabaseContract.SQL_CREATE_TABLE_CONTACT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing in particular yet
    }
}
