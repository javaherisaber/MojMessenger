package ir.logicbase.mojmessenger.database.message;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM message WHERE fk_contact_id = :contactId;")
    public List<MessageEntity> getMessagesByContactId(int contactId);

    @Query("SELECT DISTINCT fk_contact_id FROM message")
    public List<Integer> getDistinctContactIds();

    @Query("SELECT * FROM message WHERE _id = :messageId;")
    public MessageEntity getMessageById(int messageId);

    @Query("SELECT * FROM message WHERE server_message_id = :serverMsgId")
    public MessageEntity getMessageByServerMsgId(int serverMsgId);

    @Query("SELECT * FROM message WHERE fk_contact_id = :contactId ORDER BY _id DESC LIMIT 1;")
    public MessageEntity getLastMessage(int contactId);

    @Query("SELECT COUNT(*) FROM message WHERE fk_contact_id = :contactId AND is_yours = 0 AND seen = 0;")
    public int countUnreadMessages(int contactId);

    @Query("SELECT server_message_id FROM message WHERE is_yours = 0 AND seen = 0 AND sync = 0")
    public List<Integer> getNotSyncedDeliverAckMessages();

    @Query("SELECT * FROM message WHERE is_yours = 0 AND seen = 1 AND sync = 0")
    public List<MessageEntity> getNotSyncedSeenMessages();

    @Query("SELECT * FROM message WHERE fk_contact_id = :contactId AND is_yours = 0 AND seen = 0 AND sync = 1")
    public List<MessageEntity> getNotSeenMessages(int contactId);

    @Query("SELECT * FROM message WHERE is_yours = 1 AND seen = 0 AND sync = 0")
    public List<MessageEntity> getNoSyncedTextMessages();

    @Query("SELECT server_message_id FROM message WHERE is_yours = 1 AND seen = 1 AND sync = 0")
    public List<Integer> getNotSyncedDeliverSeenAckMessages();

    @Query("UPDATE message SET seen = 1 WHERE _id = :messageId;")
    public void setMessageSeen(int messageId);

    @Query("UPDATE message SET sync = :sync WHERE _id = :messageId;")
    public void setMessageSync(int sync, int messageId);

    @Query("UPDATE message SET server_message_id = :serverId, time_stamp = :timestamp, sync = 1 WHERE _id = :messageId;")
    public void setMessageTimestampAndServerId(int messageId, String timestamp, int serverId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertMessage(MessageEntity messageEntity);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insertMessages(MessageEntity... messageEntities);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insertMessages(List<MessageEntity> messageEntities);

    @Delete
    public void deleteMessages(MessageEntity... messageEntities);

}
