package ir.logicbase.mojmessenger.contact;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ir.logicbase.mojmessenger.database.DatabaseBroker;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.database.contact.ContactRepository;
import ir.logicbase.mojmessenger.database.contact.LoadContactCallback;
import ir.logicbase.mojmessenger.socket.IncomingGateway;
import ir.logicbase.mojmessenger.socket.OutgoingGateway;
import ir.logicbase.mojmessenger.util.PrefManager;

/**
 * Created by Mahdi on 12/8/2017.
 * Listens for users's actions from the UI {@link FragmentHomeContactList}, retrieves the data and updates
 * the UI as required.
 */

class ContactPresenter implements IncomingGateway.ProfilePicChangeListener, IncomingGateway.LastSeenChangeListener {

    private ContactRepository repository;

    @Nullable
    private ContactView view;

    ContactPresenter(Context context, @Nullable ContactView view) {
        startListening(context, view);
    }

    void initiate() {
        repository.getAllContacts(loadContactCallback);
    }

    void syncContactsStatus(Context context) {
        PrefManager pref = new PrefManager(context);
        String selfPhone = pref.getPhoneNumber();
        HashMap<String, String> phoneContacts = ContactHelper.getAllContacts(selfPhone, context.getContentResolver());
        OutgoingGateway.syncContactsStatus(selfPhone, new ArrayList<>(phoneContacts.keySet()));
        IncomingGateway.getInstance().setSyncContactsStatusListener(contacts -> {
            repository.updateServerContacts(phoneContacts, contacts, contacts1 -> {
                if (view != null) {
                    view.updateContactLists(contacts1);
                }
            });
        });
    }

    void stopListening() {
        view = null;
    }

    void startListening(Context context, ContactView view) {
        this.repository = DatabaseBroker.getInstance(context).getContactRepository();
        this.view = view;
        IncomingGateway.getInstance().setProfilePicChangeListener(this);
        IncomingGateway.getInstance().setLastSeenChangeListener(this);
    }

    private LoadContactCallback loadContactCallback = new LoadContactCallback() {
        @Override
        public void onAllContactsInitiated(List<ContactEntity> contacts) {
            if (view != null) {
                view.initiateAllContacts(contacts);
            }
        }

        @Override
        public void onDataNotAvailable() {
            if (view != null) {
                view.initiateAllContacts(new ArrayList<>());  // empty list
            }
        }
    };

    @Override
    public void onProfilePicChanged(ContactEntity contact) {
        if (view != null) {
            view.updateContact(contact);
        }
    }

    @Override
    public void onLastSeenChanged(ContactEntity contact) {
        if (view != null) {
            view.updateContact(contact);
        }
    }
}
