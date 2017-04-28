package ir.logicbase.mojmessenger.database.chat;

/**
 * Created by Mahdi on 12/15/2017.
 * When chat loaded from repository
 */

public interface LoadChatCallback {

    void onChatLoaded(ChatEntity chat);
}
