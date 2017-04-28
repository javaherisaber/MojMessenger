package ir.logicbase.mojmessenger.database.message;

/**
 * Created by Mahdi on 12/9/2017.
 * Callback called when the MessageEntity was inserted into the repository.
 */

public interface InsertMessageCallback {

    /**
     * message inserted into database
     * @param message the inserted message
     */
    void onMessageInserted(MessageEntity message);
}
