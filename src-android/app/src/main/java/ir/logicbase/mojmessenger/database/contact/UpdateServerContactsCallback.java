package ir.logicbase.mojmessenger.database.contact;

import java.util.List;

/**
 * Created by Mahdi on 12/15/2017.
 * When server contacts updated with database
 */

public interface UpdateServerContactsCallback {

    void onServerContactsUpdated(List<ContactEntity> contacts);
}
