package ir.logicbase.mojmessenger.database.message;

/**
 * Created by Mahdi on 12/13/2017.
 * Callback called when our friend seen our message
 */

public interface UpdateSeenColumnCallback {

    void onSeenColumnSubmitted(MessageEntity message);
}
