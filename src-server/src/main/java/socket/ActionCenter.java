package socket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import database.DatabaseGateway;
import entity.ClientEntity;
import entity.MessageEntity;
import entity.UserEntity;
import otp.OTP;
import sms.SmsHelper;
import util.Time;

import javax.xml.crypto.Data;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Mahdi on 8/25/2017.
 * centralized decision maker for incoming messages and events
 */
class ActionCenter {

    static void processRequest(HashMap<String, String> header, byte[] body, ClientTask client) {
        String method = header.get(MessageHelper.HEADER_KEY_METHOD);
        switch (method) {
            case MessageHelper.HEADER_VALUE_METHOD_HEART_BEAT:
                sendHeartbeat(client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_OTP_REQUEST:
                OTPRequest(body, client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_OTP_VERIFY:
                OTPVerify(body, client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_REGISTER:
                registerNewUser(body, client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_TEXT_MESSAGE:
                textMessage(header.get(MessageHelper.HEADER_KEY_SENDER_PHONE),
                        header.get(MessageHelper.HEADER_KEY_RECIPIENT_PHONE),
                        body,
                        client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_GO_ONLINE:
                goOnline(body, client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_GO_OFFLINE:
                goOffline(body, client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SYNC_CONTACTS:
                syncContacts(header.get(MessageHelper.HEADER_KEY_SENDER_PHONE), body, client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SEEN_MESSAGE:
                seenMessage(header.get(MessageHelper.HEADER_KEY_SENDER_PHONE),
                        header.get(MessageHelper.HEADER_KEY_RECIPIENT_PHONE),
                        body, client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_DELIVER_MESSAGE_ACK:
                deliverMessageAck(body);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_DELIVER_SEEN_ACK:
                deliverSeenAck(body);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SYNC_PROFILE:
                syncProfile(body, client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SYNC_CONTACTS_STATUS:
                syncContactsStatus(header.get(MessageHelper.HEADER_KEY_SENDER_PHONE), body, client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SYNC_DELIVER_SEEN_MESSAGES:
                syncDeliverSeenMessages(header.get(MessageHelper.HEADER_KEY_SENDER_PHONE), client);
                break;
            case MessageHelper.HEADER_VALUE_METHOD_SYNC_DELIVER_TEXT_MESSAGES:
                syncDeliverTextMessages(header.get(MessageHelper.HEADER_KEY_SENDER_PHONE), client);
                break;
        }
    }

    private static void syncDeliverTextMessages(String phone, ClientTask client) {
        List<MessageEntity> messages = DatabaseGateway.getNotDeliveredTextMessages(phone);
        for (MessageEntity message : messages) {
            UserEntity user = DatabaseGateway.getUserById(message.getSenderUserId());
            if (user != null) {
                deliverTextMessage(user.getPhone(), message, client);
            }
        }
    }

    private static void syncDeliverSeenMessages(String phone, ClientTask client) {
        List<Integer> serverMsgIds = DatabaseGateway.getNotDeliveredSeenMessages(phone);
        for (int id : serverMsgIds) {
            deliverSeen(id, client);
        }
    }

    private static void syncContactsStatus(String senderPhone, byte[] body, ClientTask client) {
        syncContacts(senderPhone, MessageHelper.HEADER_VALUE_METHOD_SYNC_CONTACTS_STATUS, body, client);
    }

    private static void syncProfile(byte[] body, ClientTask client) {
        // parse request
        JsonObject requestRoot = MessageHelper.byteArrayToJsonObject(body);
        String contactPhone = requestRoot.get("contactPhone").getAsString();

        // get user from database
        UserEntity user = DatabaseGateway.getUserByPhone(contactPhone);

        // send response
        if (user != null) {
            JsonObject responseRoot = new JsonObject();
            responseRoot.addProperty("contactPhone", contactPhone);
            responseRoot.addProperty("pic", user.getPic());
            responseRoot.addProperty("lastSeen", user.getLastSeen());
            if (ServerSocketBroker.connectedClients.containsKey(contactPhone) &&
                    ServerSocketBroker.connectedClients.get(contactPhone).isOnline()) {
                responseRoot.addProperty("isOnline", true);
            } else {
                responseRoot.addProperty("isOnline", false);
            }
            byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
            HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(
                    MessageHelper.HEADER_VALUE_METHOD_SYNC_PROFILE, responseBody.length);
            byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
            client.addToOutbox(responsePacket);
        }

    }

    private static void deliverSeenAck(byte[] body) {
        // parse request
        JsonObject requestRoot = MessageHelper.byteArrayToJsonObject(body);
        int serverMsgId = requestRoot.get("serverMsgId").getAsInt();

        DatabaseGateway.updateMessageRecipientSync(serverMsgId, 1);  // set recipientSync to 1
    }

    private static void seenMessage(String sender, String recipient, byte[] body, ClientTask client) {
        // parse request
        JsonObject requestRoot = MessageHelper.byteArrayToJsonObject(body);
        int serverMsgId = requestRoot.get("serverMsgId").getAsInt();

        // save to database
        DatabaseGateway.updateMessageSeen(serverMsgId, 1);  // set seen to 1
        DatabaseGateway.updateMessageSenderSync(serverMsgId, 0);  // set senderSync to 0
        DatabaseGateway.updateMessageRecipientSync(serverMsgId, 0);  // set recipientSync to 0


        // send seen_message_ack => recipient = request client
        if (ServerSocketBroker.connectedClients.containsKey(recipient)) {
            DatabaseGateway.updateMessageSenderSync(serverMsgId, 1);  // set senderSync to 1
            JsonObject responseRoot = new JsonObject();
            responseRoot.addProperty("serverMsgId", serverMsgId);
            byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
            HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(MessageHelper.HEADER_VALUE_METHOD_SEEN_MESSAGE_ACK,
                    responseBody.length);
            byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
            client.addToOutbox(responsePacket);
        }

        // send deliver_seen
        if (ServerSocketBroker.connectedClients.containsKey(sender)) {
            ClientTask senderClient = ServerSocketBroker.connectedClients.get(sender).getTask();
            deliverSeen(serverMsgId, senderClient);
        }
    }

    private static void deliverSeen(int serverMsgId, ClientTask senderClient) {
        JsonObject responseRoot = new JsonObject();
        responseRoot.addProperty("serverMsgId", serverMsgId);
        byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(MessageHelper.HEADER_VALUE_METHOD_DELIVER_SEEN,
                responseBody.length);
        byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
        senderClient.addToOutbox(responsePacket);
    }

    private static void deliverMessageAck(byte[] body) {
        // parse request
        JsonObject requestRoot = MessageHelper.byteArrayToJsonObject(body);
        int serverMsgId = requestRoot.get("serverMsgId").getAsInt();

        // save to database
        DatabaseGateway.updateMessageRecipientSync(serverMsgId, 1);  // set recipientSync to 1
    }

    private static void textMessage(String sender, String recipient, byte[] body, ClientTask client) {
        // parse request
        JsonObject requestRoot = MessageHelper.byteArrayToJsonObject(body);
        String message = requestRoot.get("message").getAsString();
        String messageId = requestRoot.get("messageId").getAsString();

        // save to database
        int senderUserId = DatabaseGateway.getUserIDByPhone(sender);
        int recipientUserId = DatabaseGateway.getUserIDByPhone(recipient);
        String timestamp = Time.getUTCTimestamp();
        int serverMsgId = DatabaseGateway.insertNewMessage(senderUserId, recipientUserId, timestamp, message);

        // text_message_ack
        if (ServerSocketBroker.connectedClients.containsKey(sender)) {
            DatabaseGateway.updateMessageSenderSync(serverMsgId, 1);  // set senderSync to 1
            JsonObject responseRoot = new JsonObject();
            responseRoot.addProperty("messageId", messageId);
            responseRoot.addProperty("serverMsgId", serverMsgId);
            responseRoot.addProperty("timestamp", timestamp);
            byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
            HashMap<String, String> responseHeaders = MessageHelper.getCommandHeaderWithRecipient(
                    MessageHelper.HEADER_VALUE_METHOD_TEXT_MESSAGE_ACK, responseBody.length, recipient);
            byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
            client.addToOutbox(responsePacket);
        }

        // deliver message
        if (ServerSocketBroker.connectedClients.containsKey(recipient)) {
            ClientEntity entity = ServerSocketBroker.connectedClients.get(recipient);
            deliverTextMessage(sender, new MessageEntity(serverMsgId, message, timestamp, senderUserId), entity.getTask());
        }
    }

    private static void deliverTextMessage(String sender, MessageEntity message, ClientTask task) {
        JsonObject deliverResponse = new JsonObject();
        deliverResponse.addProperty("message", message.getContent());
        deliverResponse.addProperty("serverMsgId", message.getServerMsgId());
        deliverResponse.addProperty("timestamp", message.getTimestamp());
        byte[] deliverBody = deliverResponse.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> deliverHeaders = MessageHelper.getCommandHeaderWithSender(
                MessageHelper.HEADER_VALUE_METHOD_DELIVER_MESSAGE, deliverBody.length, sender);
        byte[] deliverPacket = MessageHelper.createPacket(deliverHeaders, deliverBody);
        task.addToOutbox(deliverPacket);
    }

    private static void sendHeartbeat(ClientTask client) {
        byte[] responsePacket = MessageHelper.createPacket(MessageHelper.getCommandHeader(
                MessageHelper.HEADER_VALUE_METHOD_HEART_BEAT, 0), new byte[0]);
        client.addToOutbox(responsePacket);
    }

    private static void syncContacts(String senderPhone, byte[] body, ClientTask client) {
        syncContacts(senderPhone, MessageHelper.HEADER_VALUE_METHOD_SYNC_CONTACTS, body, client);
    }

    private static void syncContacts(String senderPhone, String method, byte[] body, ClientTask client) {
        // parse request
        JsonArray requestRoot = MessageHelper.byteArrayToJsonArray(body);
        String[] numbers = new String[requestRoot.size()];
        for (int i = 0; i < requestRoot.size(); i++) {
            String phone = requestRoot.get(i).getAsString();
            numbers[i] = phone;
        }
        ArrayList<UserEntity> contacts = DatabaseGateway.getUsersByNumbers(numbers);
        // create response
        JsonArray responseRoot = new JsonArray();
        if (contacts != null) {
            // insert new contacts to user contact table
            int fkUserId = DatabaseGateway.getUserIDByPhone(senderPhone);
            ArrayList<Integer> contactUserIdList = contacts.stream().map(entity ->
                    entity.getId()).collect(Collectors.toCollection(ArrayList::new));
            DatabaseGateway.insertContacts(fkUserId, contactUserIdList);
            // create response
            for (UserEntity entity :
                    contacts) {
                JsonObject object = new JsonObject();
                object.addProperty("phone", entity.getPhone());
                object.addProperty("pic", entity.getPic());
                if (ServerSocketBroker.connectedClients.containsKey(entity.getPhone()) &&
                        ServerSocketBroker.connectedClients.get(entity.getPhone()).isOnline()) {
                    object.addProperty("isOnline", true);
                } else {
                    object.addProperty("isOnline", false);
                }
                object.addProperty("lastSeen", entity.getLastSeen());
                responseRoot.add(object);
            }
        }
        byte[] response = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
        byte[] responsePacket = MessageHelper.createPacket(MessageHelper.getCommandHeader(method, response.length), response);
        client.addToOutbox(responsePacket);
    }

    private static void goOffline(byte[] body, ClientTask client) {
        // parse request
        JsonObject requestRoot = MessageHelper.byteArrayToJsonObject(body);
        String phone = requestRoot.get("phone").getAsString();

        // update fields of server and database
        ClientEntity entity = ServerSocketBroker.connectedClients.get(phone);
        entity.setOnline(false);
        DatabaseGateway.updateUserLastSeen(phone);

        // inform sender's friends
        sendLastSeenChange(phone, false);

        // create response
        JsonObject responseRoot = new JsonObject();
        responseRoot.addProperty("status", "ok");
        byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(
                MessageHelper.HEADER_VALUE_METHOD_GO_OFFLINE, responseBody.length);
        byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
        client.addToOutbox(responsePacket);
    }

    private static void goOnline(byte[] body, ClientTask client) {
        // parse request
        JsonObject requestRoot = MessageHelper.byteArrayToJsonObject(body);
        String phone = requestRoot.get("phone").getAsString();

        // update fields of server
        ServerSocketBroker.connectedClients.put(phone, new ClientEntity(client, true));

        // inform sender's friends
        sendLastSeenChange(phone, true);

        // create response
        JsonObject responseRoot = new JsonObject();
        responseRoot.addProperty("status", "ok");
        byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(
                MessageHelper.HEADER_VALUE_METHOD_GO_ONLINE, responseBody.length);
        byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
        client.addToOutbox(responsePacket);
    }

    public static void sendLastSeenChange(String senderPhone, boolean isOnline) {
        ArrayList<String> friendsPhone = DatabaseGateway.getUserFriendsPhone(senderPhone);
        friendsPhone.stream().filter(ServerSocketBroker.connectedClients::containsKey).forEach(friendPhone -> {
            if (ServerSocketBroker.connectedClients.get(friendPhone).isOnline()) {
                ClientTask friendTask = ServerSocketBroker.connectedClients.get(friendPhone).getTask();
                if (friendTask != null) {
                    JsonObject responseRoot = new JsonObject();
                    responseRoot.addProperty("ContactPhone", senderPhone);
                    responseRoot.addProperty("lastSeen", Time.getUTCTimestamp());
                    responseRoot.addProperty("isOnline", isOnline);
                    byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
                    HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(
                            MessageHelper.HEADER_VALUE_METHOD_CHANGE_LAST_SEEN, responseBody.length);
                    byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
                    friendTask.addToOutbox(responsePacket);
                }
            }
        });
    }

    private static void registerNewUser(byte[] body, ClientTask client) {
        // parse request
        JsonObject requestRoot = MessageHelper.byteArrayToJsonObject(body);
        String phone = requestRoot.get("phone").getAsString();
        int result = DatabaseGateway.createNewUser(phone);
        // create response
        JsonObject responseRoot = new JsonObject();
        if (result != -1) {
            // user exist
            responseRoot.addProperty("status", "ok");
        } else {
            responseRoot.addProperty("status", "error");
        }
        byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(
                MessageHelper.HEADER_VALUE_METHOD_REGISTER, responseBody.length);
        byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
        client.addToOutbox(responsePacket);
    }

    private static void OTPVerify(byte[] body, ClientTask client) {
        // parse request
        JsonObject requestRoot = MessageHelper.byteArrayToJsonObject(body);
        String otpCode = requestRoot.get("otpCode").getAsString();
        boolean isValid = OTP.isValidOTP(Integer.parseInt(otpCode));
        // create response
        JsonObject responseRoot = new JsonObject();
        if (isValid) {
            responseRoot.addProperty("status", "ok");
        } else {
            responseRoot.addProperty("status", "error");
        }
        byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(
                MessageHelper.HEADER_VALUE_METHOD_OTP_VERIFY, responseBody.length);
        byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
        client.addToOutbox(responsePacket);
    }

    private static void OTPRequest(byte[] body, ClientTask client) {
        // parse request
        JsonObject requestRoot = MessageHelper.byteArrayToJsonObject(body);
        String phone = requestRoot.get("phone").getAsString();
        // send otp sms
        int otp = OTP.generateOTP();
        String text = SmsHelper.TEXT_SEND_OTP + otp;
        boolean result = SmsHelper.sendOTPSms(phone, text);
        // create response
        JsonObject responseRoot = new JsonObject();
        if (result) {
            responseRoot.addProperty("status", "ok");
        } else {
            responseRoot.addProperty("status", "error");
        }
        byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(
                MessageHelper.HEADER_VALUE_METHOD_OTP_REQUEST, responseBody.length);
        byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
        client.addToOutbox(responsePacket);
    }
}
