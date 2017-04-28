package ir.logicbase.mojmessenger.database.contact;

/**
 * Created by Mahdi on 12/13/2017.
 * Callback called when contact exist by this phone
 */

public interface GetContactCallback {

    void onContactAvailable(ContactEntity contact);
}
