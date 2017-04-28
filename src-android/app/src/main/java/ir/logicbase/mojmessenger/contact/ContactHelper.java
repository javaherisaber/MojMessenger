package ir.logicbase.mojmessenger.contact;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mahdi on 8/1/2017.
 * - CRUD operations on contacts
 */

public class ContactHelper {

    /**
     * @return True if operation successful or False if contact already exist
     */
    static boolean insertNewContact(ContentResolver resolver, String name, String phone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        // attach custom account
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // add name to contact
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        // add phone to contact
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            return false;  // contact already registered
        }
        return true;
    }

    /**
     * @return HashMap&lt;PhoneNumber, DisplayName&gt;
     */
    public static HashMap<String, String> getAllContacts(String selfPhone, ContentResolver resolver) {
        HashMap<String, String> contacts = new HashMap<>();
        try (Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null)) {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    // retrieve contact data
                    String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumber = clearPhoneFormat(phoneNumber);
                    if (!isValidPhone(phoneNumber)) {
                        continue;
                    }
                    contacts.put(phoneNumber, displayName);
                } while (cursor.moveToNext());
            }
            // remove self phone from list
            if (contacts.containsKey(selfPhone)) {
                contacts.remove(selfPhone);
            }
        }
        return contacts;
    }

    /**
     * only name could be changed
     *
     * @param phone this is the fixed old phone number of contact and cant change
     * @return True if operation successful or False if no such contact or error
     */
    static boolean updateContact(ContentResolver resolver, String newName, String phone) {
        long contactId = getContactID(resolver, phone);
        if (contactId == -1)
            return false;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " +
                                ContactsContract.Data.MIMETYPE + "='" +
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'",
                        new String[]{String.valueOf(contactId)})
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newName)
                .build());
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            return false;
        } catch (OperationApplicationException e) {
            return false;
        }
        return true;
    }

    /**
     * caution :: this method will delete all entries of this contact
     *
     * @return True if operation successful or False if no such contact or error
     */
    static boolean deleteContact(ContentResolver resolver, String phone) {
        long contactId = getContactID(resolver, phone);
        if (contactId == -1)
            return false;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?",
                        new String[]{String.valueOf(contactId)})
                .build());
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            return false;
        } catch (OperationApplicationException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if this contact exist at all (either registered or not)
     */
    static boolean isContactExistByThisPhone(ContentResolver resolver, String phone) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            return true;
        } else {
            if (cursor != null) {
                cursor.close();
            }
            return false;
        }
    }

    /**
     * @return contact ID associated with this phone
     */
    private static long getContactID(ContentResolver resolver, String phone) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        String[] projection = {ContactsContract.PhoneLookup._ID};
        Cursor cursor = null;

        try {
            cursor = resolver.query(contactUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int personID = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
                return cursor.getLong(personID);
            }
            return -1;
        } catch (Exception e) {
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * @return create valid format based on protocol (eg. +989302871049)
     */
    private static String clearPhoneFormat(String phone) {
        phone = phone.replaceAll(" ", "");
        phone = phone.replaceAll("-", "");
        char[] phoneArray = phone.toCharArray();
        if (phoneArray[0] == '0') {
            phone = "+98" + new String(phoneArray, 1, phoneArray.length - 1);
        }
        return phone;
    }

    public static boolean isValidPhone(String phone) {
        String regex = "^(\\+98|0)\\d{10}$";
        return phone.matches(regex);
    }

}
