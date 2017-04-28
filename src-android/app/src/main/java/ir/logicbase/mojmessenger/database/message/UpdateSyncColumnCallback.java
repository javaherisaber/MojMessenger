package ir.logicbase.mojmessenger.database.message;

/**
 * Created by Mahdi on 12/14/2017.
 * When a change going to happen to sync column
 */

public interface UpdateSyncColumnCallback {

    void onSyncColumnSubmitted(MessageEntity message);
}
