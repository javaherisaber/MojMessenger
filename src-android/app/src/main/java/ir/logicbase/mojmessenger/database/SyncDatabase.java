package ir.logicbase.mojmessenger.database;


import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ir.logicbase.mojmessenger.contact.ContactHelper;
import ir.logicbase.mojmessenger.database.contact.ContactRepository;
import ir.logicbase.mojmessenger.database.contact.SyncContactsCallback;
import ir.logicbase.mojmessenger.database.message.MessageEntity;
import ir.logicbase.mojmessenger.database.message.MessageRepository;
import ir.logicbase.mojmessenger.socket.ConnectionHandler;
import ir.logicbase.mojmessenger.socket.IncomingGateway;
import ir.logicbase.mojmessenger.socket.OutgoingGateway;
import ir.logicbase.mojmessenger.util.AppLifecycleTracker;
import ir.logicbase.mojmessenger.util.ApplicationLoader;
import ir.logicbase.mojmessenger.util.PrefManager;

/**
 * Created by Mahdi on 12/15/2017.
 * Singleton -> powered by "initialization on demand" in order to ensure thread-safety and performance
 * sync contacts and messages with server
 */

public class SyncDatabase {

    /*
     * prevent from instantiation
     */
    private SyncDatabase() {
    }

    /*
     * powered by "initialization on demand" idiom
     */
    private static class lazyHolder {
        static final SyncDatabase instance = new SyncDatabase();
    }

    /**
     * @return the same instance of class every time
     */
    public static SyncDatabase getInstance() {
        return SyncDatabase.lazyHolder.instance;
    }

    private SyncDatabaseListener syncDatabaseListener;
    private boolean isSyncing = false;

    public interface SyncDatabaseListener {
        void syncing();

        void syncCompleted();
    }

    public void setSyncDatabaseListener(SyncDatabaseListener listener) {
        this.syncDatabaseListener = listener;
    }

    public void startSyncing() {
        if (!isSyncing && ConnectionHandler.getInstance().isSocketConnected()) {
            sync();
        }
    }

    public void sync() {
        isSyncing = true;
        Context context = ApplicationLoader.getInstance();
        PrefManager pref = new PrefManager(context);
        if (pref.getIsRegistered()) {

            if (syncDatabaseListener != null) {
                syncDatabaseListener.syncing();
            }

            // sync data with server on Network thread
            if (ConnectionHandler.getInstance().isSocketConnected()) {
                syncContacts(context, contacts -> syncMessages(context, () -> {
                    // when sync complete
                    isSyncing = false;
                    if (syncDatabaseListener != null) {
                        syncDatabaseListener.syncCompleted();
                    }

                    // this go online is for when connection reset
                    if (AppLifecycleTracker.isAppInForeground()) {
                        ConnectionHandler c = ConnectionHandler.getInstance();
                        if (!c.wantOnline() && !c.isOnline()) {
                            c.setWantOnline(true);  // go online
                            c.interrupt();
                            Log.d("MojMessengerNetwork", "SyncDatabase want goOnline");
                        }
                    }
                }));
            }
        }
    }

    /**
     * Sync phone contacts with server and notify via the callback
     *
     * @param callback gets called when contacts synced
     */
    private void syncContacts(Context context, final SyncContactsCallback callback) {
        PrefManager pref = new PrefManager(context);
        String selfPhone = pref.getPhoneNumber();
        HashMap<String, String> phoneContacts = ContactHelper.getAllContacts(selfPhone, context.getContentResolver());

        OutgoingGateway.sendSyncContacts(selfPhone, new ArrayList<>(phoneContacts.keySet()));

        if (ConnectionHandler.getInstance().isSocketConnected()) {
            IncomingGateway.getInstance().setSyncContactsListener((serverContacts) -> {
                ContactRepository repository = DatabaseBroker.getInstance(context).getContactRepository();
                repository.updateServerContacts(phoneContacts, serverContacts, contacts -> {
                    if (callback != null) {
                        callback.onContactsSynced(contacts);
                    }
                });
            });
        }
    }

    private void syncMessages(Context context, final SyncMessagesCallback callback) {

        PrefManager pref = new PrefManager(context);
        String selfPhone = pref.getPhoneNumber();
        MessageRepository repository = DatabaseBroker.getInstance(context).getMessageRepository();
        ContactRepository contactRepository = DatabaseBroker.getInstance(context).getContactRepository();
        repository.getNotSyncedDeliverAckMessages(serverMsgIds -> {
            for (int id : serverMsgIds) {
                if (ConnectionHandler.getInstance().isSocketConnected()) {
                    repository.setMessageSync(1, id, message1 -> {
                        OutgoingGateway.sendDeliverMessageAck(id);
                    });
                }
            }
        });
        repository.getNotSyncedDeliverSeenAckMessages(serverMsgIds -> {
            for (int id : serverMsgIds) {
                if (ConnectionHandler.getInstance().isSocketConnected()) {
                    repository.setMessageSync(1, id, message2 -> {
                        OutgoingGateway.sendDeliverSeenAck(id);
                    });
                }
            }
        });
        repository.getNotSyncedSeenMessages(messages -> {
            for (MessageEntity message : messages) {
                if (ConnectionHandler.getInstance().isSocketConnected()) {
                    contactRepository.getContactById(message.getFkContactId(), contact -> {
                        repository.setMessageSeen(message.getServerMessageId(), message12 -> {
                            repository.setMessageSync(0, message.getServerMessageId(), message13 -> {
                                OutgoingGateway.seenMessage(contact.getPhone(), selfPhone, message.getServerMessageId());
                            });
                        });
                    });
                }
            }
        });
        OutgoingGateway.syncDeliverSeenMessages(selfPhone);
        repository.getNoSyncedTextMessages(messages -> {
            for (MessageEntity message : messages) {
                contactRepository.getContactById(message.getFkContactId(), contact -> {
                    OutgoingGateway.sendTextMessage(selfPhone, contact.getPhone(), message.getContent(), message.getId());
                });
            }
        });
        OutgoingGateway.syncDeliverTextMessages(selfPhone);
        if (callback != null) {
            callback.onSyncCompleted();
        }

    }

    private interface SyncMessagesCallback {
        void onSyncCompleted();
    }

}
