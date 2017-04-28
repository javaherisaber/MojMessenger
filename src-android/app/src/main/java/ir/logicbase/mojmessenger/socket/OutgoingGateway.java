package ir.logicbase.mojmessenger.socket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mahdi on 11/17/2017.
 * anyone who wants to send any message should use this gateway
 */

public class OutgoingGateway {

    static void sendHeartbeat() {
        byte[] body = new byte[0];
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_HEART_BEAT, 0);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void sendGoOnline(String phone) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty("phone", phone);
        byte[] body = rootObject.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_GO_ONLINE, body.length);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void sendGoOffline(String phone) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty("phone", phone);
        byte[] body = rootObject.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_GO_OFFLINE, body.length);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void sendRequestOTP(String phone) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty("phone", phone);
        byte[] body = rootObject.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_OTP_REQUEST, body.length);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void sendOTPVerify(String otpCode) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty("otpCode", otpCode);
        byte[] body = rootObject.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_OTP_VERIFY, body.length);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void sendRegister(String phone) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty("phone", phone);
        byte[] body = rootObject.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_REGISTER, body.length);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void sendSyncContacts(String senderPhone, ArrayList<String> phoneList) {
        JsonArray rootArray = new JsonArray();
        for (String phone : phoneList) {
            rootArray.add(phone);
        }
        byte[] body = rootArray.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_SYNC_CONTACTS, body.length,
                senderPhone);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void sendTextMessage(String sender, String recipient, String message, int messageId) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty("messageId", messageId);
        rootObject.addProperty("message", message);
        byte[] body = rootObject.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_TEXT_MESSAGE, body.length,
                sender, recipient);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void seenMessage(String sender, String recipient, int serverMsgId) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty("serverMsgId", serverMsgId);
        byte[] body = rootObject.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_SEEN_MESSAGE, body.length,
                sender, recipient);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void sendDeliverSeenAck(int serverMsgId) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty("serverMsgId", serverMsgId);
        byte[] body = rootObject.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_DELIVER_SEEN_ACK, body.length);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void sendDeliverMessageAck(int serverMsgId) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty("serverMsgId", serverMsgId);
        byte[] body = rootObject.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_DELIVER_MESSAGE_ACK, body.length);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void syncProfile(String contactPhone) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty("contactPhone", contactPhone);
        byte[] body = rootObject.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_SYNC_PROFILE, body.length);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void syncContactsStatus(String senderPhone, ArrayList<String> phoneList) {
        JsonArray rootArray = new JsonArray();
        for (String phone : phoneList) {
            rootArray.add(phone);
        }
        byte[] body = rootArray.toString().getBytes(Charset.forName("UTF-8"));
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_SYNC_CONTACTS_STATUS,
                body.length, senderPhone);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void syncDeliverSeenMessages(String phone) {
        byte[] body = new byte[0];
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_SYNC_DELIVER_SEEN_MESSAGES,
                body.length, phone);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }

    public static void syncDeliverTextMessages(String phone) {
        byte[] body = new byte[0];
        HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_SYNC_DELIVER_TEXT_MESSAGES,
                body.length, phone);
        SocketBroker.getInstance().sendMessage(new PacketEntity(header, body));
    }
}
