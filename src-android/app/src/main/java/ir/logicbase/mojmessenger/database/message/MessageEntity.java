package ir.logicbase.mojmessenger.database.message;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.util.Time;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "message",
        foreignKeys = @ForeignKey(
                entity = ContactEntity.class,
                parentColumns = "_id",
                childColumns = "fk_contact_id",
                onDelete = CASCADE),
        indices = {@Index(value = {"fk_contact_id"})})
public class MessageEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = "server_message_id")
    private int serverMessageId;

    @ColumnInfo(name = "fk_contact_id")
    private int fkContactId;

    @ColumnInfo(name = "is_yours")
    private int isYours;

    @ColumnInfo(name = "time_stamp")
    private String timestamp;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "seen")
    private int seen;

    @ColumnInfo(name = "sync")
    private int sync;

    public MessageEntity(int id, int serverMessageId, int fkContactId, int isYours, String timestamp, String content, int seen, int sync) {
        this.id = id;
        this.serverMessageId = serverMessageId;
        this.fkContactId = fkContactId;
        this.isYours = isYours;
        this.timestamp = timestamp;
        this.content = content;
        this.seen = seen;
        this.sync = sync;
    }

    /**
     * transfer message from user input to database
     */
    @Ignore
    public MessageEntity(int fkContactId, String content) {
        this(0, 0, fkContactId, 1, Time.getLocalTimestamp(), content, 0, 0);
    }

    /**
     * transfer message from server to database
     */
    @Ignore
    public MessageEntity(int fkContactId, int serverMessageId, String timestamp, String content) {
        this(0, serverMessageId, fkContactId, 0, timestamp, content, 0, 0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerMessageId() {
        return serverMessageId;
    }

    public void setServerMessageId(int serverMessageId) {
        this.serverMessageId = serverMessageId;
    }

    public int getFkContactId() {
        return fkContactId;
    }

    public void setFkContactId(int fkContactId) {
        this.fkContactId = fkContactId;
    }

    public int getIsYours() {
        return isYours;
    }

    public void setIsYours(int isYours) {
        this.isYours = isYours;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public int getSync() {
        return sync;
    }

    public void setSync(int sync) {
        this.sync = sync;
    }
}
