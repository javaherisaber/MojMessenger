package ir.logicbase.mojmessenger.database.message;

import android.support.annotation.MainThread;

import java.util.List;

/**
 * Callback called when the MessageEntity was loaded from the repository.
 */
public interface LoadMessageCallback {
    /**
     * Messages loaded from database
     *
     * @param messages the messages from repository.
     */
    @MainThread
    void onMessagesInitiated(int contactId, List<MessageEntity> messages);

    /**
     * there was no MessageEntity in the repository.
     */
    @MainThread
    void onDataNotAvailable(int contactId);
}
