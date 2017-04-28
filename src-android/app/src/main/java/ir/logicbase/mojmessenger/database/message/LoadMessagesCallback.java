package ir.logicbase.mojmessenger.database.message;

import java.util.List;

/**
 * Created by Mahdi on 12/15/2017.
 * called when bunch of messages loaded from repository
 */

public interface LoadMessagesCallback {

    void onMessagesLoaded(List<MessageEntity> messages);
}
