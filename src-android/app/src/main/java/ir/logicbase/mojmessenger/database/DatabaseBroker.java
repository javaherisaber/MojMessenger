package ir.logicbase.mojmessenger.database;

import android.content.Context;
import android.support.annotation.NonNull;

import ir.logicbase.mojmessenger.database.contact.ContactDao;
import ir.logicbase.mojmessenger.database.contact.ContactRepository;
import ir.logicbase.mojmessenger.database.message.MessageDao;
import ir.logicbase.mojmessenger.database.message.MessageRepository;
import ir.logicbase.mojmessenger.util.AppExecutors;

/**
 * Created by Mahdi on 12/8/2017.
 * Main entry to access database and its repositories
 */

public final class DatabaseBroker {

    private static volatile DatabaseBroker INSTANCE;

    private ContactDao contactDao;
    private MessageDao messageDao;

    private AppExecutors appExecutors;

    private DatabaseBroker(ContactDao contactDao, MessageDao messageDao) {
        this.contactDao = contactDao;
        this.messageDao = messageDao;
        appExecutors = new AppExecutors();
    }

    public static DatabaseBroker getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseBroker.class) {
                if (INSTANCE == null) {
                    MojDatabase database = MojDatabase.getInstance(context);
                    INSTANCE = new DatabaseBroker(database.contactDao(), database.messageDao());
                }
            }
        }
        return INSTANCE;
    }

    public ContactRepository getContactRepository() {
        return new ContactRepository(appExecutors, contactDao);
    }

    public MessageRepository getMessageRepository() {
        return new MessageRepository(appExecutors, contactDao, messageDao);
    }
}
