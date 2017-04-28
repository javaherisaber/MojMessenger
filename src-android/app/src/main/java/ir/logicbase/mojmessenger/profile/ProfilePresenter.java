package ir.logicbase.mojmessenger.profile;

import android.support.annotation.Nullable;

import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.socket.IncomingGateway;
import ir.logicbase.mojmessenger.socket.OutgoingGateway;

/**
 * Created by Mahdi on 12/12/2017.
 * Listens for users's actions from the UI {@link ActivityProfile}, retrieves the data and updates
 * the UI as required.
 */

class ProfilePresenter implements IncomingGateway.LastSeenChangeListener, IncomingGateway.ProfilePicChangeListener,
        IncomingGateway.SyncProfileListener {

    @Nullable
    private ProfileView view;

    ProfilePresenter(@Nullable ProfileView view) {
        startListening(view);
    }

    void stopListening() {
        view = null;
    }

    void startListening(ProfileView view) {
        this.view = view;
        IncomingGateway.getInstance().setProfilePicChangeListener(this);
        IncomingGateway.getInstance().setLastSeenChangeListener(this);
        IncomingGateway.getInstance().setSyncProfileListener(this);
    }

    void syncProfile(String contactPhone) {
        OutgoingGateway.syncProfile(contactPhone);
    }

    @Override
    public void onLastSeenChanged(ContactEntity contact) {
        if (view != null) {
            view.updateContact(contact);
        }
    }

    @Override
    public void onProfilePicChanged(ContactEntity contact) {
        if (view != null) {
            view.updateContact(contact);
        }
    }

    @Override
    public void onProfileSynced(ContactEntity contact) {
        if (view != null) {
            view.updateContact(contact);
        }
    }
}
