package ir.logicbase.mojmessenger.socket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import ir.logicbase.mojmessenger.database.DatabaseBroker;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.database.contact.ContactRepository;
import ir.logicbase.mojmessenger.database.message.MessageEntity;
import ir.logicbase.mojmessenger.database.message.MessageRepository;
import ir.logicbase.mojmessenger.util.ApplicationLoader;

/**
 * Created by Mahdi on 7/24/2017.
 * Singleton -> powered by "initialization on demand" in order to ensure thread-safety and performance
 * a place where messages arrive from network
 */

public final class IncomingGateway {

    /*
     * prevent from instantiation
     */
    private IncomingGateway() {
    }

    /*
     * powered by "initialization on demand" idiom
     */
    private static class lazyHolder {
        static final IncomingGateway instance = new IncomingGateway();
    }

    /**
     * @return the same instance of class every time
     */
    public static IncomingGateway getInstance() {
        return IncomingGateway.lazyHolder.instance;
    }

    private SendOTPListener sendOTPListener;
    private VerifyOTPListener verifyOTPListener;
    private RegisterNewUserListener registerNewUserListener;
    private GoOnlineListener goOnlineListener;
    private SyncContactsListener syncContactsListener;
    private UploadPhotoListener uploadPhotoListener;
    private ProfilePicChangeListener profilePicChangeListener;
    private LastSeenChangeListener lastSeenChangeListener;
    private TextMessageAckListener textMessageAckListener;
    private DeliverSeenListener deliverSeenListener;
    private DeliverMessageListener deliverMessageListener;
    private SyncProfileListener syncProfileListener;
    private SyncContactsStatusListener syncContactsStatusListener;

    public void setSyncContactsListener(SyncContactsListener listener) {
        this.syncContactsListener = listener;
    }

    public void setGoOnlineListener(GoOnlineListener listener) {
        this.goOnlineListener = listener;
    }

    public void setRegisterNewUserListener(RegisterNewUserListener listener) {
        this.registerNewUserListener = listener;
    }

    public void setVerifyOTPListener(VerifyOTPListener listener) {
        this.verifyOTPListener = listener;
    }

    public void setSendOTPListener(SendOTPListener listener) {
        this.sendOTPListener = listener;
    }

    public void setUploadPhotoListener(UploadPhotoListener listener) {
        this.uploadPhotoListener = listener;
    }

    public void setProfilePicChangeListener(ProfilePicChangeListener listener) {
        this.profilePicChangeListener = listener;
    }

    public void setLastSeenChangeListener(LastSeenChangeListener listener) {
        this.lastSeenChangeListener = listener;
    }

    public void setTextMessageAckListener(TextMessageAckListener listener) {
        this.textMessageAckListener = listener;
    }

    public void setDeliverSeenListener(DeliverSeenListener listener) {
        this.deliverSeenListener = listener;
    }

    public void setDeliverMessageListener(DeliverMessageListener listener) {
        this.deliverMessageListener = listener;
    }

    public void setSyncProfileListener(SyncProfileListener listener) {
        this.syncProfileListener = listener;
    }

    public void setSyncContactsStatusListener(SyncContactsStatusListener listener) {
        this.syncContactsStatusListener = listener;
    }

    public interface SyncContactsStatusListener {
        void onContactsStatusReceived(List<ContactEntity> contacts);
    }

    public interface SyncProfileListener {
        void onProfileSynced(ContactEntity contact);
    }

    public interface DeliverMessageListener {
        void onDeliverMessage(MessageEntity message);
    }

    public interface DeliverSeenListener {
        void onSeenArrive(MessageEntity message);
    }

    public interface TextMessageAckListener {
        void onTextMessageAckArrive(MessageEntity message);
    }

    public interface LastSeenChangeListener {
        void onLastSeenChanged(ContactEntity contact);
    }

    public interface ProfilePicChangeListener {
        void onProfilePicChanged(ContactEntity contact);
    }

    public interface UploadPhotoListener {
        void onUploadSuccessful(String photoUrl);
    }

    public interface RegisterNewUserListener {
        void onUserRegistered(boolean isSuccessful);
    }

    public interface SyncContactsListener {
        void onContactsSynced(List<ContactEntity> contacts);
    }

    public interface GoOnlineListener {
        void onGoingOnline();

        void onGoingOffline();
    }

    public interface VerifyOTPListener {
        void onOTPVerified(boolean isCorrectOTP);
    }

    public interface SendOTPListener {
        void onOTPSent(boolean isSuccessful);
    }

    void onMessageArrive(PacketEntity message) {
        String method = message.getHeaders().get(MessageHelper.HEADER_KEY_METHOD);
        switch (method) {
            case MessageHelper.HEADER_VALUE_METHOD_OTP_REQUEST:
                OTPRequest(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_OTP_VERIFY:
                OTPVerify(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_REGISTER:
                registerNewUser(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_GO_ONLINE:
                goOnline(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_GO_OFFLINE:
                goOffline(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SYNC_CONTACTS:
                syncContacts(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_UPLOAD_PHOTO:
                uploadPhoto(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_CHANGE_PROFILE_PIC:
                changeProfilePic(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_CHANGE_LAST_SEEN:
                changeLastSeen(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_TEXT_MESSAGE_ACK:
                textMessageAck(message.getHeaders().get(MessageHelper.HEADER_KEY_RECIPIENT_PHONE),
                        message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SEEN_MESSAGE_ACK:
                seenMessageAck(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_DELIVER_MESSAGE:
                deliverMessage(message.getHeaders().get(MessageHelper.HEADER_KEY_SENDER_PHONE),
                        message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_DELIVER_SEEN:
                deliverSeen(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SYNC_PROFILE:
                syncProfile(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SYNC_CONTACTS_STATUS:
                syncContactsStatus(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SYNC_DELIVER_SEEN_MESSAGES:
                syncDeliverSeenMessages(message.getBody());
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SYNC_DELIVER_TEXT_MESSAGES:
                syncDeliverTextMessages(message.getBody());
                break;
        }
    }

    private void syncContactsStatus(byte[] body) {
        JsonArray root = MessageHelper.byteArrayToJsonArray(body);
        ArrayList<ContactEntity> contacts = new ArrayList<>();
        for (int i = 0; i < root.size(); i++) {
            JsonObject object = root.get(i).getAsJsonObject();
            String phone = object.get("phone").getAsString();
            String pic = object.get("pic").getAsString();
            String lastSeen = object.get("lastSeen").getAsString();
            boolean isOnline = object.get("isOnline").getAsBoolean();
            contacts.add(new ContactEntity(phone, pic, lastSeen, isOnline));
        }
        if (syncContactsStatusListener != null) {
            syncContactsStatusListener.onContactsStatusReceived(contacts);
        }
    }

    private void syncProfile(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        String contactPhone = root.get("contactPhone").getAsString();
        String pic = root.get("pic").getAsString();
        String lastSeen = root.get("lastSeen").getAsString();
        boolean isOnline = root.get("isOnline").getAsBoolean();
        ContactRepository repository = DatabaseBroker.getInstance(ApplicationLoader.getInstance()).getContactRepository();
        repository.getContactByPhone(contactPhone, contact -> {
            contact.setPic(pic);
            contact.setLastSeen(lastSeen);
            contact.setOnline(isOnline);
            repository.updateContact(contact, contact1 -> {
                if (syncProfileListener != null) {
                    syncProfileListener.onProfileSynced(contact);
                }
            });
        });
    }

    private void syncDeliverTextMessages(byte[] body) {
        JsonArray root = MessageHelper.byteArrayToJsonArray(body);
        for (int i = 0; i < root.size(); i++) {
            JsonObject object = root.get(i).getAsJsonObject();
            String sender = object.get("sender").getAsString();
            String content = object.get("message").getAsString();
            int serverMsgId = object.get("serverMsgId").getAsInt();
            String timestamp = object.get("timestamp").getAsString();
            deliverMessage(sender, content, serverMsgId, timestamp);
        }
    }

    private void deliverMessage(String sender, byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        String content = root.get("message").getAsString();
        int serverMsgId = root.get("serverMsgId").getAsInt();
        String timestamp = root.get("timestamp").getAsString();
        deliverMessage(sender, content, serverMsgId, timestamp);
    }

    private void deliverMessage(String sender, String content, int serverMsgId, String timestamp) {
        ContactRepository contactRepository = DatabaseBroker.getInstance(ApplicationLoader.getInstance()).getContactRepository();
        contactRepository.getContactByPhone(sender, contact -> {
            MessageRepository messageRepository = DatabaseBroker.getInstance(ApplicationLoader.getInstance()).getMessageRepository();
            messageRepository.insertMessage(new MessageEntity(contact.getId(), serverMsgId, timestamp, content),
                    (message) -> {
                        if (ConnectionHandler.getInstance().isSocketConnected()) {
                            messageRepository.setMessageSync(1, serverMsgId, message1 -> {
                                OutgoingGateway.sendDeliverMessageAck(serverMsgId);
                            });
                        }
                        if (deliverMessageListener != null) {
                            deliverMessageListener.onDeliverMessage(message);
                        } else {
                            // TODO: 12/13/2017 show notification
                        }
                    });
        });
    }

    private void syncDeliverSeenMessages(byte[] body) {
        JsonArray root = MessageHelper.byteArrayToJsonArray(body);
        int[] serverMsgIds = new int[root.size()];
        for (int i = 0; i < root.size(); i++) {
            int id = root.get(i).getAsInt();
            serverMsgIds[i] = id;
        }
        for (int serverMsgId : serverMsgIds) {
            deliverSeen(serverMsgId);
        }
    }

    private void deliverSeen(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        int serverMsgId = root.get("serverMsgId").getAsInt();
        deliverSeen(serverMsgId);
    }

    private void deliverSeen(int serverMsgId) {
        MessageRepository repository = DatabaseBroker.getInstance(ApplicationLoader.getInstance()).getMessageRepository();
        repository.setMessageSeen(serverMsgId, message -> {
            repository.setMessageSync(0, serverMsgId, message1 -> {
                if (ConnectionHandler.getInstance().isSocketConnected()) {
                    repository.setMessageSync(1, serverMsgId, message2 -> {
                        OutgoingGateway.sendDeliverSeenAck(serverMsgId);
                    });
                }
                if (deliverSeenListener != null) {
                    deliverSeenListener.onSeenArrive(message);
                }
            });
        });
    }

    private void seenMessageAck(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        int serverMsgId = root.get("serverMsgId").getAsInt();
        MessageRepository repository = DatabaseBroker.getInstance(ApplicationLoader.getInstance()).getMessageRepository();
        repository.setMessageSync(1, serverMsgId, null);
    }

    private void textMessageAck(String recipient, byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        int id = root.get("messageId").getAsInt();
        int serverMsgId = root.get("serverMsgId").getAsInt();
        String timestamp = root.get("timestamp").getAsString();
        ContactRepository contactRepository = DatabaseBroker.getInstance(ApplicationLoader.getInstance()).getContactRepository();
        contactRepository.getContactByPhone(recipient, (contact -> {
            // update server parts of message and set sync to 1
            MessageRepository repository = DatabaseBroker.getInstance(ApplicationLoader.getInstance()).getMessageRepository();
            repository.initServerFields(id, serverMsgId, timestamp, message -> {
                if (textMessageAckListener != null) {
                    textMessageAckListener.onTextMessageAckArrive(message);
                }
            });
        }));
    }

    private void changeProfilePic(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        String phone = root.get("ContactPhone").getAsString();
        String picUrl = root.get("PicUrl").getAsString();
        boolean isOnline = root.get("isOnline").getAsBoolean();
        ContactRepository repository = DatabaseBroker.getInstance(ApplicationLoader.getInstance()).getContactRepository();
        repository.updateContactPhoto(phone, picUrl, contact -> {
            if (profilePicChangeListener != null) {
                contact.setOnline(isOnline);
                profilePicChangeListener.onProfilePicChanged(contact);
            }
        });
    }

    private void changeLastSeen(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        String phone = root.get("ContactPhone").getAsString();
        String picUrl = root.get("lastSeen").getAsString();
        boolean isOnline = root.get("isOnline").getAsBoolean();
        ContactRepository repository = DatabaseBroker.getInstance(ApplicationLoader.getInstance()).getContactRepository();
        repository.updateContactLastSeen(phone, picUrl, contact -> {
            if (lastSeenChangeListener != null) {
                contact.setOnline(isOnline);
                lastSeenChangeListener.onLastSeenChanged(contact);
            }
        });
    }

    private void uploadPhoto(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        String result = root.get("status").getAsString();
        if (result.equals("ok")) {
            if (uploadPhotoListener != null) {
                String path = root.get("url").getAsString();
                uploadPhotoListener.onUploadSuccessful(path);
            }
        }
    }

    private void syncContacts(byte[] body) {
        JsonArray root = MessageHelper.byteArrayToJsonArray(body);
        ArrayList<ContactEntity> contacts = new ArrayList<>();
        for (int i = 0; i < root.size(); i++) {
            JsonObject object = root.get(i).getAsJsonObject();
            String phone = object.get("phone").getAsString();
            String pic = object.get("pic").getAsString();
            String lastSeen = object.get("lastSeen").getAsString();
            boolean isOnline = object.get("isOnline").getAsBoolean();
            contacts.add(new ContactEntity(phone, pic, lastSeen, isOnline));
        }
        if (syncContactsListener != null) {
            syncContactsListener.onContactsSynced(contacts);
        }
    }

    private void goOffline(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        String result = root.get("status").getAsString();
        if (result.equals("ok")) {
            if (goOnlineListener != null) {
                goOnlineListener.onGoingOffline();
            }
        }
    }

    private void goOnline(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        String result = root.get("status").getAsString();
        if (result.equals("ok")) {
            if (goOnlineListener != null) {
                goOnlineListener.onGoingOnline();
            }
        }
    }

    private void OTPRequest(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        String result = root.get("status").getAsString();
        if (sendOTPListener != null) {
            if (result.equals("ok")) {
                sendOTPListener.onOTPSent(true);
            } else {
                sendOTPListener.onOTPSent(false);
            }
        }
    }

    private void OTPVerify(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        String result = root.get("status").getAsString();
        if (verifyOTPListener != null) {
            if (result.equals("ok")) {
                verifyOTPListener.onOTPVerified(true);
            } else {
                verifyOTPListener.onOTPVerified(false);
            }
        }
    }

    private void registerNewUser(byte[] body) {
        JsonObject root = MessageHelper.byteArrayToJsonObject(body);
        String result = root.get("status").getAsString();
        if (registerNewUserListener != null) {
            if (result.equals("ok")) {
                registerNewUserListener.onUserRegistered(true);
            } else {
                registerNewUserListener.onUserRegistered(false);
            }
        }
    }
}
