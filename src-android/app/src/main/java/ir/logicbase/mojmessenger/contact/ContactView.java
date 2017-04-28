package ir.logicbase.mojmessenger.contact;

import java.util.List;

import ir.logicbase.mojmessenger.database.contact.ContactEntity;

/**
 * Created by Mahdi on 12/8/2017.
 * Specify the contract between the view and the presenter
 */

interface ContactView {

    void initiateAllContacts(List<ContactEntity> contacts);

    void updateContactLists(List<ContactEntity> contacts);

    void updateContact(ContactEntity contact);

}
