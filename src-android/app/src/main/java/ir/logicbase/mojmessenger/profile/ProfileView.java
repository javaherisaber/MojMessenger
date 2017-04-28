package ir.logicbase.mojmessenger.profile;

import ir.logicbase.mojmessenger.database.contact.ContactEntity;

/**
 * Created by Mahdi on 12/12/2017.
 * Specify the contract between the view and the presenter
 */

public interface ProfileView {

    void updateContact(ContactEntity contact);
}
