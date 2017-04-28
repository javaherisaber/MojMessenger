package ir.logicbase.mojmessenger.database;

import android.provider.BaseColumns;

/**
 * Created by Mahdi on 8/8/2017.
 * Database schema, tables, columns, SQL Statements used across the app
 * constant definitions for the URIs, column names, MIME types, and other meta-data that pertain to the provider
 */

@SuppressWarnings("HardCodedStringLiteral")
public final class DatabaseContract {

//    /** The authority for the contacts provider */
//    static final String AUTHORITY = "ir.logicbase.mojmessenger.provider";
//    /** A content:// style uri to the authority for the contacts provider */
//    static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    // prevent from instantiation
    private DatabaseContract() {
    }

    // TABLE Chat
    public static class Chat implements BaseColumns {
        public static final String TABLE_NAME = "chat";
        public static final String COLUMN_FK_CONTACT_ID = "contact_id";
    }

    // TABLE Message
    public static class Message implements BaseColumns {
        public static final String TABLE_NAME = "message";
        public static final String COLUMN_SERVER_MESSAGE_ID = "server_message_id";
        public static final String COLUMN_FK_CHAT_ID = "fk_chat_id";
        public static final String COLUMN_IS_YOURS = "is_yours";
        public static final String COLUMN_TIME_STAMP = "time_stamp";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_SEEN = "seen";
        public static final String COLUMN_SYNC = "sync";
    }

    // TABLE Contact
    public static class Contact implements BaseColumns {
        public static final String TABLE_NAME = "contact";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_PIC = "pic";
        public static final String COLUMN_LAST_SEEN = "last_seen";
    }

    public static class SqliteSequence {
        public static final String TABLE_NAME = "sqlite_sequence";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SEQ = "seq";
    }

    static final String SQL_CREATE_TABLE_CHAT = "CREATE TABLE IF NOT EXISTS " + Chat.TABLE_NAME + " (" +
            Chat._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Chat.COLUMN_FK_CONTACT_ID + " INTEGER);";

    static final String SQL_CREATE_TABLE_MESSAGE = "CREATE TABLE IF NOT EXISTS " + Message.TABLE_NAME + " (" +
            Message._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Message.COLUMN_SERVER_MESSAGE_ID + " INTEGER," +
            Message.COLUMN_FK_CHAT_ID + " INTEGER," +
            Message.COLUMN_IS_YOURS+ " INTEGER," +
            Message.COLUMN_TIME_STAMP + " TEXT," +
            Message.COLUMN_CONTENT + " TEXT," +
            Message.COLUMN_SEEN + " INTEGER," +
            Message.COLUMN_SYNC + " INTEGER);";

    static final String SQL_CREATE_TABLE_CONTACT = "CREATE TABLE IF NOT EXISTS " + Contact.TABLE_NAME + " (" +
            Contact._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Contact.COLUMN_NAME + " TEXT," +
            Contact.COLUMN_PHONE + " TEXT," +
            Contact.COLUMN_PIC + " TEXT," +
            Contact.COLUMN_LAST_SEEN + " TEXT," +
            "CONSTRAINT contact_unique UNIQUE (" +
            Contact.COLUMN_PHONE + "));";

    private static String SQL_COUNT_TABLE_ROWS = "SELECT COUNT(" + BaseColumns._ID + ") AS cnt FROM %s;";
    private static String SQL_SELECT_STAR_TABLE = "SELECT * FROM %s;";
    private static String SQL_INSERT_OR_REPLACE_CONTACT = "INSERT OR REPLACE INTO " + Contact.TABLE_NAME +
            " (" + BaseColumns._ID + ", " + Contact.COLUMN_PHONE + ", " + Contact.COLUMN_NAME + ", " + Contact.COLUMN_PIC +
            ", " + Contact.COLUMN_LAST_SEEN + ") VALUES ((SELECT " + BaseColumns._ID +
            " FROM " + Contact.TABLE_NAME + " WHERE " + Contact.COLUMN_PHONE + " = '%s'), '%s', '%s', '%s', '%s');";
    private static String SQL_GET_CONTACT_ID = "SELECT " + BaseColumns._ID + " FROM " + Contact.TABLE_NAME + " WHERE " +
            Contact.COLUMN_PHONE + " = '%s';";
    private static String SQL_GET_CHAT_ID = "SELECT " +BaseColumns._ID + " FROM " + Chat.TABLE_NAME + " WHERE " +
            Chat.COLUMN_FK_CONTACT_ID + " = %s;";
    private static String SQL_INSERT_CHAT = "INSERT INTO " + Chat.TABLE_NAME + " (" + Chat.COLUMN_FK_CONTACT_ID + ") " +
            "VALUES (%s);";
    private static String SQL_INSERT_MESSAGE = "INSERT INTO " + Message.TABLE_NAME + " (" + Message.COLUMN_SERVER_MESSAGE_ID + ", " +
            Message.COLUMN_FK_CHAT_ID + ", " + Message.COLUMN_IS_YOURS + ", " + Message.COLUMN_TIME_STAMP + ", " + Message.COLUMN_CONTENT +
            ", " + Message.COLUMN_SEEN + ", " + Message.COLUMN_SYNC + ") VALUES (%s, %s, %s, '%s', '%s', %s, %s);";
    private static String SQL_GET_LAST_ID = "SELECT " + SqliteSequence.COLUMN_SEQ + " FROM " + SqliteSequence.TABLE_NAME +
            " WHERE " + SqliteSequence.COLUMN_NAME + " = '%s';";
    private static String SQL_GET_ALL_MESSAGES = "SELECT * FROM " + Message.TABLE_NAME + " WHERE " + Message.COLUMN_FK_CHAT_ID + " = %s;";
    private static String SQL_SELECT_STAR_BY_ID = "SELECT * FROM %s WHERE " + BaseColumns._ID + " = %s;";
    private static String SQL_GET_LAST_MESSAGE = "SELECT * FROM " + Message.TABLE_NAME + " WHERE " + Message.COLUMN_FK_CHAT_ID +
            " = %s" + " ORDER BY " + BaseColumns._ID + " DESC LIMIT 1;";
    private static String SQL_COUNT_UNREAD_MESSAGES = "SELECT COUNT(*) AS cnt FROM " + Message.TABLE_NAME + " WHERE "
            + Message.COLUMN_FK_CHAT_ID + " = %s AND " + Message.COLUMN_IS_YOURS + " = 0 AND " + Message.COLUMN_SEEN + " = 0;";

    static String getSqlCountUnreadMessages(int fkChatId) {
        return String.format(SQL_COUNT_UNREAD_MESSAGES, fkChatId);
    }

    static String getSqlGetLastMessage(int fkChatId) {
        return String.format(SQL_GET_LAST_MESSAGE, fkChatId);
    }

    static String getSqlSelectStarById(String tableName, int id) {
        return String.format(SQL_SELECT_STAR_BY_ID, tableName, id);
    }

    static String getSqlGetAllMessages(int fkChatId) {
        return String.format(SQL_GET_ALL_MESSAGES, fkChatId);
    }

    static String getSqlGetLastId(String tableName) {
        return String.format(SQL_GET_LAST_ID, tableName);
    }

    static String getSqlInsertMessage(int serverMsgId, int fkChatId, int isYours, String timestamp, String content, int seen, int sync) {
        return String.format(SQL_INSERT_MESSAGE, serverMsgId, fkChatId, isYours, timestamp, content, seen, sync);
    }

    static String getSqlInsertChat(int fkContactId) {
        return String.format(SQL_INSERT_CHAT, fkContactId);
    }

    static String getSqlGetChatId(int fkContactId) {
        return String.format(SQL_GET_CHAT_ID, fkContactId);
    }

    static String getSqlGetContactId(String phone) {
        return String.format(SQL_GET_CONTACT_ID, phone);
    }

    static String getSqlInsertOrReplaceContact(String name, String phone, String pic, String lastSeen) {
        return String.format(SQL_INSERT_OR_REPLACE_CONTACT, phone, phone, name, pic, lastSeen);
    }

    static String getSQLSelectStarTable(String tableName) {
        return String.format(SQL_SELECT_STAR_TABLE, tableName);
    }

    static String getSQLCountTableRows(String tableName) {
        return String.format(SQL_COUNT_TABLE_ROWS, tableName);
    }
}
