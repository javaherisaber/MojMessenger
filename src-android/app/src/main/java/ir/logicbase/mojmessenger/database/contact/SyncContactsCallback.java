package ir.logicbase.mojmessenger.database.contact;

import java.util.List;

/**
 * Created by Mahdi on 12/15/2017.
 * When contacts synced gets called
 */

public interface SyncContactsCallback {

    void onContactsSynced(List<ContactEntity> contacts);
}
