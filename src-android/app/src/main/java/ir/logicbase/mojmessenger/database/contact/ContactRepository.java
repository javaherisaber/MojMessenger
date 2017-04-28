package ir.logicbase.mojmessenger.database.contact;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ir.logicbase.mojmessenger.util.AppExecutors;

/**
 * Created by Mahdi on 12/8/2017.
 * The repository is responsible of handling user data operations.
 */

public class ContactRepository {

    private AppExecutors appExecutors;

    private ContactDao contactDao;

    public ContactRepository(AppExecutors executors, ContactDao contactDao) {
        this.appExecutors = executors;
        this.contactDao = contactDao;
    }

    /**
     * Get contacts from the data source and notify via the callback that contacts has been retrieved.
     *
     * @param callback callback that gets called when contacts was retrieved from the data source.
     */
    public void getAllContacts(final LoadContactCallback callback) {
        final WeakReference<LoadContactCallback> loadContactCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            final List<ContactEntity> contacts = contactDao.getAllContacts();
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final LoadContactCallback contactCallback = loadContactCallback.get();
                if (contactCallback == null) {
                    return;
                }
                if (contacts == null) {
                    contactCallback.onDataNotAvailable();
                } else {
                    contactCallback.onAllContactsInitiated(contacts);
                }
            });
        });
    }

    public void updateServerContacts(HashMap<String, String> phoneContacts, List<ContactEntity> serverContacts,
                                     final UpdateServerContactsCallback callback) {
        final WeakReference<UpdateServerContactsCallback> updateContactCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            ArrayList<ContactEntity> allContacts = new ArrayList<>();
            for (ContactEntity contact : serverContacts) {
                String name = phoneContacts.get(contact.getPhone());
                ContactEntity tmp = new ContactEntity(name, contact.getPhone(), contact.getPic(), contact.getLastSeen(), contact.isOnline());
                ContactEntity dbContact = contactDao.getContactByPhone(contact.getPhone());
                if (dbContact == null) {
                    long id = contactDao.insertContact(tmp);
                    tmp.setId((int) id);
                } else {
                    int id = dbContact.getId();
                    tmp.setId(id);
                    contactDao.updateContacts(tmp);
                }
                allContacts.add(tmp);
            }
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final UpdateServerContactsCallback updateCallback = updateContactCallback.get();
                if (updateCallback != null) {
                    updateCallback.onServerContactsUpdated(allContacts);
                }
            });
        });
    }

    public void updateContact(ContactEntity contact, final UpdateContactCallback callback) {
        final WeakReference<UpdateContactCallback> updateContactCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            contactDao.updateContacts(contact);
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final UpdateContactCallback updateCallback = updateContactCallback.get();
                if (updateCallback != null) {
                    updateCallback.onContactUpdated(contact);
                }
            });
        });
    }

    public void updateContactPhoto(@NonNull String phone, @NonNull String pic, final UpdateContactCallback callback) {
        final WeakReference<UpdateContactCallback> updateContactCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            ContactEntity contact = contactDao.getContactByPhone(phone);
            if (contact != null) {
                contact.setPic(pic);
                contactDao.updateContacts(contact);
                // notify on the main thread
                appExecutors.mainThread().execute(() -> {
                    final UpdateContactCallback updateCallback = updateContactCallback.get();
                    if (updateCallback != null) {
                        updateCallback.onContactUpdated(contact);
                    }
                });
            }
        });
    }

    public void updateContactLastSeen(@NonNull String phone, @NonNull String lastSeen, final UpdateContactCallback callback) {
        final WeakReference<UpdateContactCallback> updateContactCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            ContactEntity contact = contactDao.getContactByPhone(phone);
            if (contact != null) {
                contact.setLastSeen(lastSeen);
                contactDao.updateContacts(contact);
                // notify on the main thread
                appExecutors.mainThread().execute(() -> {
                    final UpdateContactCallback updateCallback = updateContactCallback.get();
                    if (updateCallback != null) {
                        updateCallback.onContactUpdated(contact);
                    }
                });
            }
        });
    }

    public void getContactByPhone(String phone, final GetContactCallback callback) {
        final WeakReference<GetContactCallback> getContactCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            ContactEntity contact = contactDao.getContactByPhone(phone);
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final GetContactCallback contactCallback = getContactCallback.get();
                if (contactCallback != null) {
                    if (contact != null) {
                        contactCallback.onContactAvailable(contact);
                    }
                }
            });
        });
    }

    public void getContactById(int contactId, final GetContactCallback callback) {
        final WeakReference<GetContactCallback> getContactCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        appExecutors.diskIO().execute(() -> {
            ContactEntity contact = contactDao.getContactById(contactId);
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final GetContactCallback contactCallback = getContactCallback.get();
                if (contactCallback != null) {
                    if (contact != null) {
                        contactCallback.onContactAvailable(contact);
                    }
                }
            });
        });
    }
}
