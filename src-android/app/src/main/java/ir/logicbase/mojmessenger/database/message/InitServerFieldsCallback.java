package ir.logicbase.mojmessenger.database.message;

/**
 * Created by Mahdi on 12/13/2017.
 * Callback called when serverMessageId and timestamp of server was initiated into repository
 */

public interface InitServerFieldsCallback {

    void onServerFieldsInitiated(MessageEntity message);
}
