package ir.logicbase.mojmessenger.chat;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ir.logicbase.mojmessenger.database.DatabaseBroker;
import ir.logicbase.mojmessenger.database.chat.ChatEntity;
import ir.logicbase.mojmessenger.database.chat.LoadChatsCallback;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.database.message.MessageEntity;
import ir.logicbase.mojmessenger.database.message.MessageRepository;
import ir.logicbase.mojmessenger.socket.IncomingGateway;

/**
 * Created by Mahdi on 12/9/2017.
 * Listens for users's actions from the UI {@link FragmentHomeChatList}, retrieves the data and updates
 * the UI as required.
 */

class ChatPresenter implements IncomingGateway.LastSeenChangeListener, IncomingGateway.ProfilePicChangeListener,
        IncomingGateway.DeliverMessageListener {

    private MessageRepository repository;

    @Nullable
    private ChatView view;

    ChatPresenter(Context context, @Nullable ChatView view) {
        startListening(context, view);
    }

    void startListening(Context context, ChatView view) {
        this.repository = DatabaseBroker.getInstance(context).getMessageRepository();
        this.view = view;
        IncomingGateway.getInstance().setProfilePicChangeListener(this);
        IncomingGateway.getInstance().setLastSeenChangeListener(this);
        IncomingGateway.getInstance().setDeliverMessageListener(this);
    }

    void stopListening() {
        view = null;
    }

    void initiateChatList() {
        repository.initiateChats(loadChatsCallback);
    }

    private LoadChatsCallback loadChatsCallback = new LoadChatsCallback() {
        @Override
        public void onChatsInitiated(List<ChatEntity> chats) {
            if (view != null) {
                view.initializeChatList(chats);
            }
        }

        @Override
        public void onDataNotAvailable() {
            if (view != null) {
                view.initializeChatList(new ArrayList<>());  // empty list
            }
        }
    };

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
    public void onDeliverMessage(MessageEntity message) {
        repository.LoadChat(message.getFkContactId(), chat -> {
            if (view != null) {
                view.updateChat(chat);
            }
        });
    }
}
