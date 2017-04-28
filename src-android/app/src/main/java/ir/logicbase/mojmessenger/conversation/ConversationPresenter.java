package ir.logicbase.mojmessenger.conversation;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ir.logicbase.mojmessenger.database.DatabaseBroker;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.database.message.LoadMessageCallback;
import ir.logicbase.mojmessenger.database.message.MessageEntity;
import ir.logicbase.mojmessenger.database.message.MessageRepository;
import ir.logicbase.mojmessenger.socket.ConnectionHandler;
import ir.logicbase.mojmessenger.socket.IncomingGateway;
import ir.logicbase.mojmessenger.socket.OutgoingGateway;
import ir.logicbase.mojmessenger.util.PrefManager;

/**
 * Created by Mahdi on 12/9/2017.
 * Listens for users's actions from the UI {@link ActivityConversation}, retrieves the data and updates
 * the UI as required.
 */

class ConversationPresenter implements IncomingGateway.ProfilePicChangeListener, IncomingGateway.LastSeenChangeListener,
        IncomingGateway.TextMessageAckListener, IncomingGateway.DeliverMessageListener, IncomingGateway.DeliverSeenListener,
        IncomingGateway.SyncProfileListener {

    private MessageRepository repository;

    @Nullable
    private ConversationView view;

    ConversationPresenter(Context context, @Nullable ConversationView view) {
        startListening(context, view);
    }

    void initiateMessages(int contactId) {
        repository.initiateMessages(contactId, loadMessageCallback);
    }

    void syncProfile(String contactPhone) {
        OutgoingGateway.syncProfile(contactPhone);
    }

    void sendMessage(String sender, String recipient, MessageEntity message) {
        repository.insertMessage(message, (message1) -> {
            OutgoingGateway.sendTextMessage(sender, recipient, message1.getContent(), message1.getId());
            if (view != null) {
                view.insertNewMessage(message1);
            }
        });
    }

    void seenMessages(Context context, int contactId, String contactPhone) {
        String selfPhone = new PrefManager(context).getPhoneNumber();
        repository.getNotSeenMessages(contactId, messages -> {
            for (MessageEntity message : messages) {
                if (ConnectionHandler.getInstance().isSocketConnected()) {
                    repository.setMessageSeen(message.getServerMessageId(), message1 -> {
                        repository.setMessageSync(0, message.getServerMessageId(), message2 -> {
                            OutgoingGateway.seenMessage(contactPhone, selfPhone, message.getServerMessageId());
                        });
                    });
                }
            }
        });
    }

    void sendSeenFlag(String sender, String recipient, int serverMsgId) {
        if (ConnectionHandler.getInstance().isSocketConnected()) {
            repository.setMessageSeen(serverMsgId, message -> repository.setMessageSync(0, serverMsgId, message1 -> {
                OutgoingGateway.seenMessage(sender, recipient, serverMsgId);
            }));
        }
    }

    void startListening(Context context, @Nullable ConversationView view) {
        this.repository = DatabaseBroker.getInstance(context).getMessageRepository();
        this.view = view;
        IncomingGateway.getInstance().setProfilePicChangeListener(this);
        IncomingGateway.getInstance().setLastSeenChangeListener(this);
        IncomingGateway.getInstance().setDeliverMessageListener(this);
        IncomingGateway.getInstance().setDeliverSeenListener(this);
        IncomingGateway.getInstance().setTextMessageAckListener(this);
        IncomingGateway.getInstance().setSyncProfileListener(this);
    }

    void stopListening() {
        view = null;
    }

    private LoadMessageCallback loadMessageCallback = new LoadMessageCallback() {
        @Override
        public void onMessagesInitiated(int contactId, List<MessageEntity> messages) {
            if (view != null) {
                view.initiateConversationList(contactId, messages);
            }
        }

        @Override
        public void onDataNotAvailable(int contactId) {
            if (view != null) {
                view.initiateConversationList(contactId, new ArrayList<>());  // empty list
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
        if (view != null) {
            view.deliverMessage(message);
        }
    }

    @Override
    public void onSeenArrive(MessageEntity message) {
        if (view != null) {
            view.seenAck(message);
        }
    }

    @Override
    public void onTextMessageAckArrive(MessageEntity message) {
        if (view != null) {
            view.textMessageAck(message);
        }
    }

    @Override
    public void onProfileSynced(ContactEntity contact) {
        if (view != null) {
            view.updateContact(contact);
        }
    }
}
