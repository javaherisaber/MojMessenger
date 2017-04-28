package ir.logicbase.mojmessenger.database.message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ir.logicbase.mojmessenger.database.chat.ChatEntity;
import ir.logicbase.mojmessenger.database.chat.LoadChatCallback;
import ir.logicbase.mojmessenger.database.chat.LoadChatsCallback;
import ir.logicbase.mojmessenger.database.contact.ContactDao;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.util.AppExecutors;

/**
 * Created by Mahdi on 12/8/2017.
 * The repository is responsible of handling user data operations.
 */

public class MessageRepository {

    private AppExecutors appExecutors;

    private ContactDao contactDao;
    private MessageDao messageDao;


    public MessageRepository(AppExecutors appExecutors, ContactDao contactDao, MessageDao messageDao) {
        this.appExecutors = appExecutors;
        this.contactDao = contactDao;
        this.messageDao = messageDao;
    }

    public void initiateChats(final LoadChatsCallback callback) {
        final WeakReference<LoadChatsCallback> loadChatCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            List<Integer> contactIds = messageDao.getDistinctContactIds();
            List<ChatEntity> chats = new ArrayList<>();
            for (int contactId : contactIds) {
                ContactEntity contact = contactDao.getContactById(contactId);
                MessageEntity lastMessage = messageDao.getLastMessage(contactId);
                int count = messageDao.countUnreadMessages(contactId);
                chats.add(new ChatEntity(contact, lastMessage.getContent(), lastMessage.getTimestamp(), count));
            }

            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final LoadChatsCallback chatCallback = loadChatCallback.get();
                if (chatCallback == null) {
                    return;
                }
                if (chats.size() == 0) {
                    chatCallback.onDataNotAvailable();
                } else {
                    chatCallback.onChatsInitiated(chats);
                }
            });
        });
    }

    public void LoadChat(int contactId, final LoadChatCallback callback) {
        final WeakReference<LoadChatCallback> loadChatCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            ContactEntity contact = contactDao.getContactById(contactId);
            MessageEntity lastMessage = messageDao.getLastMessage(contactId);
            int count = messageDao.countUnreadMessages(contactId);
            ChatEntity chat = new ChatEntity(contact, lastMessage.getContent(), lastMessage.getTimestamp(), count);
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final LoadChatCallback chatCallback = loadChatCallback.get();
                if (chatCallback != null) {
                    chatCallback.onChatLoaded(chat);
                }
            });
        });
    }

    /**
     * Get messages from the data source and notify via the callback that messages has been retrieved.
     *
     * @param callback callback that gets called when messages was retrieved from the data source.
     */
    public void initiateMessages(int contactId, final LoadMessageCallback callback) {
        final WeakReference<LoadMessageCallback> loadMessageCallback = new WeakReference<>(callback);

        // request on the I/O thread
        appExecutors.diskIO().execute(() -> {
            final List<MessageEntity> messages = messageDao.getMessagesByContactId(contactId);
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final LoadMessageCallback messageCallback = loadMessageCallback.get();
                if (messageCallback == null) {
                    return;
                }
                if (messages == null) {
                    messageCallback.onDataNotAvailable(contactId);
                } else {
                    messageCallback.onMessagesInitiated(contactId, messages);
                }
            });
        });
    }

    public void insertMessage(MessageEntity message, InsertMessageCallback callback) {
        final WeakReference<InsertMessageCallback> insertMessageCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            long id = messageDao.insertMessage(message);
            message.setId((int) id);
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final InsertMessageCallback messageCallback = insertMessageCallback.get();
                if (messageCallback == null) {
                    return;
                }
                if (id > 0) {
                    messageCallback.onMessageInserted(message);
                }
            });
        });
    }

    public void initServerFields(int messageId, int serverMsgId, String timestamp, InitServerFieldsCallback callback) {
        final WeakReference<InitServerFieldsCallback> initServerFieldsCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            messageDao.setMessageTimestampAndServerId(messageId, timestamp, serverMsgId);
            MessageEntity message = messageDao.getMessageById(messageId);
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final InitServerFieldsCallback initCallback = initServerFieldsCallback.get();
                if (initCallback == null) {
                    return;
                }
                if (message != null) {
                    initCallback.onServerFieldsInitiated(message);
                }
            });
        });
    }

    public void setMessageSeen(int serverMsgId, UpdateSeenColumnCallback callback) {
        final WeakReference<UpdateSeenColumnCallback> seenCallbackWeakReference = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            MessageEntity message = messageDao.getMessageByServerMsgId(serverMsgId);
            if (message != null) {
                messageDao.setMessageSeen(message.getId());
                message.setSeen(1);
            }
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final UpdateSeenColumnCallback updateSeenColumnCallback = seenCallbackWeakReference.get();
                if (updateSeenColumnCallback == null) {
                    return;
                }
                if (message != null) {
                    updateSeenColumnCallback.onSeenColumnSubmitted(message);
                }
            });
        });
    }

    public void setMessageSync(int sync, int serverMsgId, UpdateSyncColumnCallback callback) {
        final WeakReference<UpdateSyncColumnCallback> syncColumnCallbackWeakReference = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            MessageEntity message = messageDao.getMessageByServerMsgId(serverMsgId);
            if (message != null) {
                messageDao.setMessageSync(sync, message.getId());
                message.setSync(sync);
            }
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final UpdateSyncColumnCallback updateSyncColumnCallback = syncColumnCallbackWeakReference.get();
                if (updateSyncColumnCallback == null) {
                    return;
                }
                if (message != null) {
                    updateSyncColumnCallback.onSyncColumnSubmitted(message);
                }
            });
        });
    }

    public void getNotSeenMessages(int contactId, final LoadMessagesCallback callback) {
        final WeakReference<LoadMessagesCallback> loadMessagesCallbackWeakReference = new WeakReference<>(callback);

        // request on the I/O thread
        appExecutors.diskIO().execute(() -> {
            List<MessageEntity> messages = messageDao.getNotSeenMessages(contactId);
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final LoadMessagesCallback loadMessagesCallback = loadMessagesCallbackWeakReference.get();
                if (loadMessagesCallback == null) {
                    return;
                }
                if (messages == null) {
                    loadMessagesCallback.onMessagesLoaded(new ArrayList<>());
                } else {
                    loadMessagesCallback.onMessagesLoaded(messages);
                }
            });
        });
    }

    public void getNotSyncedDeliverAckMessages(final GetServerMsgIdsCallback callback) {
        final WeakReference<GetServerMsgIdsCallback> serverMsgIdsCallbackWeakReference = new WeakReference<>(callback);

        // request on the I/O thread
        appExecutors.diskIO().execute(() -> {
            List<Integer> ids = messageDao.getNotSyncedDeliverAckMessages();
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final GetServerMsgIdsCallback serverMsgIdsCallback = serverMsgIdsCallbackWeakReference.get();
                if (serverMsgIdsCallback == null) {
                    return;
                }
                if (ids == null) {
                    serverMsgIdsCallback.onServerMsgIdsLoaded(new ArrayList<>());
                } else {
                    serverMsgIdsCallback.onServerMsgIdsLoaded(ids);
                }
            });
        });
    }

    public void getNotSyncedDeliverSeenAckMessages(final GetServerMsgIdsCallback callback) {
        final WeakReference<GetServerMsgIdsCallback> serverMsgIdsCallbackWeakReference = new WeakReference<>(callback);

        // request on the I/O thread
        appExecutors.diskIO().execute(() -> {
            List<Integer> ids = messageDao.getNotSyncedDeliverSeenAckMessages();
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final GetServerMsgIdsCallback serverMsgIdsCallback = serverMsgIdsCallbackWeakReference.get();
                if (serverMsgIdsCallback == null) {
                    return;
                }
                if (ids == null) {
                    serverMsgIdsCallback.onServerMsgIdsLoaded(new ArrayList<>());
                } else {
                    serverMsgIdsCallback.onServerMsgIdsLoaded(ids);
                }
            });
        });
    }

    public void getNotSyncedSeenMessages(final LoadMessagesCallback callback) {
        final WeakReference<LoadMessagesCallback> loadMessagesCallbackWeakReference = new WeakReference<>(callback);

        // request on the I/O thread
        appExecutors.diskIO().execute(() -> {
            List<MessageEntity> messages = messageDao.getNotSyncedSeenMessages();
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final LoadMessagesCallback loadMessagesCallback = loadMessagesCallbackWeakReference.get();
                if (loadMessagesCallback == null) {
                    return;
                }
                if (messages == null) {
                    loadMessagesCallback.onMessagesLoaded(new ArrayList<>());
                } else {
                    loadMessagesCallback.onMessagesLoaded(messages);
                }
            });
        });
    }

    public void getNoSyncedTextMessages(final LoadMessagesCallback callback) {
        final WeakReference<LoadMessagesCallback> loadMessagesCallbackWeakReference = new WeakReference<>(callback);

        // request on the I/O thread
        appExecutors.diskIO().execute(() -> {
            List<MessageEntity> messages = messageDao.getNoSyncedTextMessages();
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final LoadMessagesCallback loadMessagesCallback = loadMessagesCallbackWeakReference.get();
                if (loadMessagesCallback == null) {
                    return;
                }
                if (messages == null) {
                    loadMessagesCallback.onMessagesLoaded(new ArrayList<>());
                } else {
                    loadMessagesCallback.onMessagesLoaded(messages);
                }
            });
        });
    }

}
