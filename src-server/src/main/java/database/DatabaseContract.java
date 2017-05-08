package database;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * Created by Mahdi on 8/30/2017.
 * database schema
 */
public class DatabaseContract {

    private static final String SERVER_NAME = "localhost";
    private static final String DATABASE_NAME = "moj_messenger_server";
    private static final String USERNAME = "MojMessenger";
    private static final String PASSWORD = "mehdy1994";
    private static final MysqlDataSource dataSource;

    static {
        dataSource = new MysqlDataSource();
        dataSource.setServerName(SERVER_NAME);
        dataSource.setDatabaseName(DATABASE_NAME);
        dataSource.setUser(USERNAME);
        dataSource.setPassword(PASSWORD);
    }

    public static MysqlDataSource getDataSource() {
        return DatabaseContract.dataSource;
    }

    // TABLE Message
    public static class Message implements BaseColumns {
        public static final String TABLE_NAME = "message";
        public static final String COLUMN_SENDER_USER_ID = "sender_user_id";
        public static final String COLUMN_RECIPIENT_USER_ID = "recipient_user_id";
        public static final String COLUMN_TIME_STAMP = "time_stamp";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_SEEN = "seen";
        public static final String COLUMN_SENDER_SYNC = "sender_sync";
        public static final String COLUMN_RECIPIENT_SYNC = "recipient_sync";
    }

    // TABLE User
    public static class User implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_PIC = "pic";
        public static final String COLUMN_LAST_SEEN = "last_seen";
    }

    // TABLE Contact
    public static class Contact implements BaseColumns {
        public static final String TABLE_NAME = "contact";
        public static final String COLUMN_FK_USER_ID = "fk_user_id";
        public static final String COLUMN_CONTACT_USER_ID = "contact_user_id";
    }

    private static final String SQL_COUNT_USER_BY_PHONE = "SELECT COUNT(*) FROM `" + User.TABLE_NAME +
            "` WHERE " + User.COLUMN_PHONE + "='%s';";
    private static final String SQL_INSERT_NEW_USER = "INSERT INTO `" + User.TABLE_NAME +
            "`(" + User.COLUMN_PHONE + "," + User.COLUMN_PIC + "," + User.COLUMN_LAST_SEEN + ")VALUES('%s','%s','%s');";
    private static final String SQL_SELECT_USER_ID_BY_PHONE = "SELECT " + User.ID + " FROM `" + User.TABLE_NAME +
            "` WHERE " + User.COLUMN_PHONE + "='%s';";
    private static final String SQL_SELECT_USER_BY_PHONE = "SELECT * FROM `" + User.TABLE_NAME + "` WHERE " +
            User.COLUMN_PHONE + "='%s';";
    private static final String SQL_SELECT_USER_BY_ID = "SELECT * FROM `" + User.TABLE_NAME + "` WHERE " + User.ID + "=%s;";
    private static final String SQL_UPDATE_USER_LAST_SEEN = "UPDATE `" + User.TABLE_NAME +
            "` SET " + User.COLUMN_LAST_SEEN + "='%s' WHERE " + User.COLUMN_PHONE + "='%s';";
    private static final String SQL_INSERT_INTO_CONTACT = "INSERT INTO `" + Contact.TABLE_NAME + "`(" + Contact.COLUMN_FK_USER_ID
            + "," + Contact.COLUMN_CONTACT_USER_ID + ") VALUES (%s,%s);";
    private static final String SQL_UPDATE_USER_PIC = "UPDATE `" + User.TABLE_NAME + "` SET " +
            User.COLUMN_PIC + "='%s' WHERE " + User.COLUMN_PHONE + "='%s';";
    private static final String SQL_SELECT_CONTACT = "SELECT * FROM `" + Contact.TABLE_NAME + "` WHERE " +
            Contact.COLUMN_FK_USER_ID + "=%s;";
    private static final String SQL_INSERT_INTO_MESSAGE ="INSERT INTO `" + Message.TABLE_NAME + "`(" + Message.COLUMN_SENDER_USER_ID +
            "," + Message.COLUMN_RECIPIENT_USER_ID + "," + Message.COLUMN_TIME_STAMP + "," + Message.COLUMN_CONTENT +
            "," + Message.COLUMN_SEEN + "," + Message.COLUMN_SENDER_SYNC + "," + Message.COLUMN_RECIPIENT_SYNC +
            ") VALUES (%s,%s,'%s','%s',%s,%s,%s);";
    private static final String SQL_UPDATE_MESSAGE_SEEN = "UPDATE `" + Message.TABLE_NAME + "` SET " +
            Message.COLUMN_SEEN + "=%s WHERE " + Message.ID + "=%s;";
    private static final String SQL_UPDATE_MESSAGE_SENDER_SYNC = "UPDATE `" + Message.TABLE_NAME + "` SET " +
            Message.COLUMN_SENDER_SYNC + "=%s WHERE " + Message.ID + "=%s;";
    private static final String SQL_UPDATE_MESSAGE_RECIPIENT_SYNC = "UPDATE `" + Message.TABLE_NAME + "` SET " +
            Message.COLUMN_RECIPIENT_SYNC + "=%s WHERE " + Message.ID + "=%s;";
    private static final String SQL_SELECT_NOT_DELIVERED_SEEN_MESSAGES = "SELECT " + Message.ID + " FROM `" + Message.TABLE_NAME +
            "` WHERE " + Message.COLUMN_SENDER_USER_ID + "=%s AND " + Message.COLUMN_SEEN + "=1 AND " + Message.COLUMN_SENDER_SYNC +
            "=0;";
    private static final String SQL_SELECT_NOT_DELIVERED_TEXT_MESSAGE = "SELECT * FROM " + Message.TABLE_NAME + " WHERE " +
            Message.COLUMN_RECIPIENT_USER_ID + "=%s AND " + Message.COLUMN_RECIPIENT_SYNC + "=0;";


    static String getSqlSelectNotDeliveredTextMessage(int userId) {
        return String.format(SQL_SELECT_NOT_DELIVERED_TEXT_MESSAGE, userId);
    }

    static String getSqlSelectNotDeliveredSeenMessages(int userId) {
        return String.format(SQL_SELECT_NOT_DELIVERED_SEEN_MESSAGES, userId);
    }

    static String getSqlUpdateMessageRecipientSync(int id, int recipientSync) {
        return String.format(SQL_UPDATE_MESSAGE_RECIPIENT_SYNC, recipientSync, id);
    }

    static String getSqlUpdateMessageSenderSync(int id, int senderSync) {
        return String.format(SQL_UPDATE_MESSAGE_SENDER_SYNC, senderSync, id);
    }

    static String getSqlUpdateMessageSeen(int id, int seen) {
        return String.format(SQL_UPDATE_MESSAGE_SEEN, seen, id);
    }

    static String getSqlInsertIntoMessage(int senderUserId, int recipientUserId, String timestamp, String content,
                                          int seen, int senderSync, int recipientSync) {
        return String.format(SQL_INSERT_INTO_MESSAGE, senderUserId, recipientUserId, timestamp, content,
                seen, senderSync, recipientSync);
    }

    static String getSqlSelectContact(int fkUserId) {
        return String.format(SQL_SELECT_CONTACT, fkUserId);
    }

    static String getSqlUpdateUserPic(String phone, String pic) {
        return String.format(SQL_UPDATE_USER_PIC, pic, phone);
    }

    static String getSqlInsertIntoContact(int fkUserID, int contactUserId) {
        return String.format(SQL_INSERT_INTO_CONTACT, fkUserID, contactUserId);
    }

    static String getSqlUpdateUserLastSeen(String lastSeen, String phone) {
        return String.format(SQL_UPDATE_USER_LAST_SEEN, lastSeen, phone);
    }

    static String getSqlSelectUserById(int userId) {
        return String.format(SQL_SELECT_USER_BY_ID, userId);
    }

    static String getSqlSelectUserByPhone(String phone) {
        return String.format(SQL_SELECT_USER_BY_PHONE, phone);
    }

    static String getSqlSelectUserIdByPhone(String phone) {
        return String.format(SQL_SELECT_USER_ID_BY_PHONE, phone);
    }

    static String getSqlInsertNewUser(String phone, String pic, String lastSeen) {
        return String.format(SQL_INSERT_NEW_USER, phone, pic, lastSeen);
    }

    static String getSQLCountUserByPhone(String phone) {
        return String.format(SQL_COUNT_USER_BY_PHONE, phone);
    }
}
