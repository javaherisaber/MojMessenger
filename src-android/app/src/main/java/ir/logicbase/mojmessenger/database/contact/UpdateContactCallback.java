package ir.logicbase.mojmessenger.database.contact;

/**
 * Created by Mahdi on 12/10/2017.
 * Callback called when contact pic column was updated
 */

public interface UpdateContactCallback {

    void onContactUpdated(ContactEntity contact);
}
