package ir.logicbase.mojmessenger.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;

import ir.logicbase.mojmessenger.bundle.BundleChat;
import ir.logicbase.mojmessenger.bundle.BundleContact;
import ir.logicbase.mojmessenger.bundle.BundleMessage;

/**
 * Created by Mahdi on 8/8/2017.
 * entry point to access data from database
 */

public class DatabaseGateway {

    private SQLiteDatabase db;

    public DatabaseGateway(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public synchronized void closeDatabase() {
        db.close();
    }

    public boolean isTableEmpty(String tableName) {
        try (Cursor c = db.rawQuery(DatabaseContract.getSQLCountTableRows(tableName), null)) {
            c.moveToFirst();
            int cnt = c.getInt(0);
            return cnt <= 0;
        } catch (SQLException e) {
            // do nothing
        }
        return true;
    }

    public ArrayList<BundleContact> getAllContacts() {
        ArrayList<BundleContact> contacts = new ArrayList<>();
        try (Cursor c = db.rawQuery(DatabaseContract.getSQLSelectStarTable(DatabaseContract.Contact.TABLE_NAME), null)) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(BaseColumns._ID));
                String name = c.getString(c.getColumnIndex(DatabaseContract.Contact.COLUMN_NAME));
                String phone = c.getString(c.getColumnIndex(DatabaseContract.Contact.COLUMN_PHONE));
                String pic = c.getString(c.getColumnIndex(DatabaseContract.Contact.COLUMN_PIC));
                String lastSeen = c.getString(c.getColumnIndex(DatabaseContract.Contact.COLUMN_LAST_SEEN));
                contacts.add(new BundleContact(id, name, phone, pic, lastSeen, false));
            }
        } catch (SQLException e) {
            // do nothing
        }
        return contacts;
    }

    /**
     * insert new contacts or if contact exist then replace their name, pic or last seen with new ones
     */
    public void insertOrReplaceContacts(ArrayList<BundleContact> contacts) {
        for (BundleContact contact :
                contacts) {
            try {
                String sql = DatabaseContract.getSqlInsertOrReplaceContact(contact.getName(), contact.getPhone(),
                        contact.getPic(), contact.getLastSeen());
                db.execSQL(sql);
            } catch (SQLException e) {
                // do nothing
            }
        }
    }

    /**
     * @param contactId contact id
     * @param message message being sent
     * @return id of newly inserted message
     */
    public int insertMessage(int contactId, BundleMessage message) {
        return insertMessage(-1, contactId, message);
    }

    /**
     * @param contactId contact id
     * @param message message being sent
     * @param serverMsgId message id which comes from server
     * @return id of newly inserted message
     */
    public int insertMessage(int serverMsgId, int contactId, BundleMessage message) {
        try {
            Cursor c = db.rawQuery(DatabaseContract.getSqlGetChatId(contactId), null);
            c.moveToFirst();
            if (c.getCount() == 0) {
                // no chat exist insert new one
                db.execSQL(DatabaseContract.getSqlInsertChat(contactId));
                c = db.rawQuery(DatabaseContract.getSqlGetChatId(contactId), null);
                c.moveToFirst();
            }
            int chatId = c.getInt(c.getColumnIndex(BaseColumns._ID));
            db.execSQL(DatabaseContract.getSqlInsertMessage(serverMsgId, chatId, message.isYours() ? 1 : 0, message.getTimestamp(),
                    message.getMsgContent(), message.isSeen() ? 1 : 0, message.isSync() ? 1 : 0));
            c = db.rawQuery(DatabaseContract.getSqlGetLastId(DatabaseContract.Message.TABLE_NAME), null);
            c.moveToFirst();
            int lastId = c.getInt(c.getColumnIndex(DatabaseContract.SqliteSequence.COLUMN_SEQ));
            c.close();
            return lastId;
        } catch (SQLException e) {
            return -1;
        }
    }

    /**
     * get list of contacts and set their id field
     * @param contacts contacts which has no correct database id
     */
    public void setContactsIdFromDB(ArrayList<BundleContact> contacts) {
        for (BundleContact contact : contacts) {
            try {
                Cursor c = db.rawQuery(DatabaseContract.getSqlGetContactId(contact.getPhone()), null);
                c.moveToFirst();
                int contactId = c.getInt(c.getColumnIndex(BaseColumns._ID));
                c.close();
                contact.setId(contactId);
            } catch (SQLException e) {
                // do nothing
            }
        }
    }

    public ArrayList<BundleMessage> getAllMessages(int contactId) {
        try {
            Cursor c = db.rawQuery(DatabaseContract.getSqlGetChatId(contactId), null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                int chatId = c.getInt(c.getColumnIndex(BaseColumns._ID));
                c = db.rawQuery(DatabaseContract.getSqlGetAllMessages(chatId), null);
                ArrayList<BundleMessage> messages = new ArrayList<>();
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndex(BaseColumns._ID));
                    int serverMessageId = c.getInt(c.getColumnIndex(DatabaseContract.Message.COLUMN_SERVER_MESSAGE_ID));
                    boolean isYours = c.getInt(c.getColumnIndex(DatabaseContract.Message.COLUMN_IS_YOURS)) == 1;
                    String timestamp = c.getString(c.getColumnIndex(DatabaseContract.Message.COLUMN_TIME_STAMP));
                    String msgContent = c.getString(c.getColumnIndex(DatabaseContract.Message.COLUMN_CONTENT));
                    boolean seen = c.getInt(c.getColumnIndex(DatabaseContract.Message.COLUMN_SEEN)) == 1;
                    boolean sync = c.getInt(c.getColumnIndex(DatabaseContract.Message.COLUMN_SYNC)) == 1;
                    messages.add(new BundleMessage(id, serverMessageId, isYours, timestamp, msgContent, seen, sync));
                }
                c.close();
                return messages;
            } else {
                c.close();
                return new ArrayList<>();
            }
        } catch (SQLException e) {
            // do nothing
        }
        return new ArrayList<>();
    }

    public ArrayList<BundleChat> getAllChats() {
        try {
            Cursor c = db.rawQuery(DatabaseContract.getSQLSelectStarTable(DatabaseContract.Chat.TABLE_NAME), null);
            ArrayList<BundleChat> chats = new ArrayList<>();
            while (c.moveToNext()) {
                int chatId = c.getInt(c.getColumnIndex(BaseColumns._ID));
                int contactId = c.getInt(c.getColumnIndex(DatabaseContract.Chat.COLUMN_FK_CONTACT_ID));
                Cursor c2 = db.rawQuery(DatabaseContract.getSqlSelectStarById(DatabaseContract.Contact.TABLE_NAME, contactId), null);
                c2.moveToFirst();
                String name = c2.getString(c2.getColumnIndex(DatabaseContract.Contact.COLUMN_NAME));
                String phone = c2.getString(c2.getColumnIndex(DatabaseContract.Contact.COLUMN_PHONE));
                String pic = c2.getString(c2.getColumnIndex(DatabaseContract.Contact.COLUMN_PIC));
                String lastSeen = c2.getString(c2.getColumnIndex(DatabaseContract.Contact.COLUMN_LAST_SEEN));
                BundleContact contact = new BundleContact(contactId, name, phone, pic, lastSeen, false);
                c2 = db.rawQuery(DatabaseContract.getSqlGetLastMessage(chatId), null);
                c2.moveToFirst();
                String lastMsg = c2.getString(c2.getColumnIndex(DatabaseContract.Message.COLUMN_CONTENT));
                String timestamp = c2.getString(c2.getColumnIndex(DatabaseContract.Message.COLUMN_TIME_STAMP));
                c2 = db.rawQuery(DatabaseContract.getSqlCountUnreadMessages(chatId), null);
                c2.moveToFirst();
                int count = c2.getInt(0);
                chats.add(new BundleChat(contact, lastMsg, timestamp, count));
                c2.close();
            }
            c.close();
            return chats;
        } catch (SQLException e) {
            // do nothing
        }
        return new ArrayList<>();
    }

}
