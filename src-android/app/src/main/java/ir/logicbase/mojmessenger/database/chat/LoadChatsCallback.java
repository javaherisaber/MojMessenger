package ir.logicbase.mojmessenger.database.chat;

import android.support.annotation.MainThread;

import java.util.List;

/**
 * Callback called when the ChatEntity was loaded from the repository.
 */
public interface LoadChatsCallback {
    /**
     * Chats loaded from database
     *
     * @param chats the chats from repository.
     */
    @MainThread
    void onChatsInitiated(List<ChatEntity> chats);

    /**
     * there was no ChatEntity in the repository.
     */
    @MainThread
    void onDataNotAvailable();
}
