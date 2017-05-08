package socket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mahdi on 7/24/2017.
 * everything associated with message
 * -header specs
 */
class MessageHelper {

    private static final char HEADER_PAIR_SEPARATOR = ':';
    private static final char HEADER_LINE_SEPARATOR = '#';
    private static final char HEADER_END_SEPARATOR = '$';

    static final String HEADER_KEY_METHOD = "Method";
    static final String HEADER_KEY_CONTENT_LENGTH = "Content-Length";
    static final String HEADER_KEY_SENDER_PHONE = "Sender-Phone";
    static final String HEADER_KEY_RECIPIENT_PHONE = "Recipient-Phone";

    static final String HEADER_VALUE_METHOD_HEART_BEAT = "heartbeat";  // keep connection alive
    static final String HEADER_VALUE_METHOD_OTP_REQUEST = "otp_request";
    static final String HEADER_VALUE_METHOD_OTP_VERIFY = "otp_verify";
    static final String HEADER_VALUE_METHOD_REGISTER = "register";
    static final String HEADER_VALUE_METHOD_GO_ONLINE = "go_online";
    static final String HEADER_VALUE_METHOD_GO_OFFLINE = "go_offline";
    static final String HEADER_VALUE_METHOD_UPLOAD_PHOTO = "upload_photo";
    static final String HEADER_VALUE_METHOD_CHANGE_PROFILE_PIC = "change_profile_pic";
    static final String HEADER_VALUE_METHOD_CHANGE_LAST_SEEN = "change_last_seen";
    static final String HEADER_VALUE_METHOD_TEXT_MESSAGE = "text_message";
    static final String HEADER_VALUE_METHOD_TEXT_MESSAGE_ACK = "text_message_ack";
    static final String HEADER_VALUE_METHOD_DELIVER_MESSAGE = "deliver_message";
    static final String HEADER_VALUE_METHOD_DELIVER_MESSAGE_ACK = "deliver_message_ack";
    static final String HEADER_VALUE_METHOD_SEEN_MESSAGE = "seen_message";
    static final String HEADER_VALUE_METHOD_SEEN_MESSAGE_ACK = "seen_message_ack";
    static final String HEADER_VALUE_METHOD_DELIVER_SEEN = "deliver_seen";
    static final String HEADER_VALUE_METHOD_DELIVER_SEEN_ACK = "deliver_seen_ack";
    static final String HEADER_VALUE_METHOD_SYNC_PROFILE = "sync_profile";
    static final String HEADER_VALUE_METHOD_SYNC_CONTACTS = "sync_contacts";
    static final String HEADER_VALUE_METHOD_SYNC_CONTACTS_STATUS = "sync_contacts_status";
    static final String HEADER_VALUE_METHOD_SYNC_DELIVER_SEEN_MESSAGES = "sync_deliver_seen_messages";
    static final String HEADER_VALUE_METHOD_SYNC_DELIVER_TEXT_MESSAGES = "sync_deliver_text_messages";

    static HashMap<String, String> getCommandHeader(String method, int length){
        HashMap<String, String > header = new HashMap<>();
        header.put(HEADER_KEY_METHOD, method);
        header.put(HEADER_KEY_CONTENT_LENGTH, String.valueOf(length));
        return header;
    }

    static HashMap<String, String> getCommandHeaderWithRecipient(String method, int length, String recipient) {
        HashMap<String, String> header = getCommandHeader(method, length);
        header.put(HEADER_KEY_RECIPIENT_PHONE, recipient);
        return header;
    }

    static HashMap<String, String> getCommandHeaderWithSender(String method, int length, String sender) {
        HashMap<String, String> header = getCommandHeader(method, length);
        header.put(HEADER_KEY_SENDER_PHONE, sender);
        return header;
    }

    static JsonObject byteArrayToJsonObject(byte[] value) {
        String bodyContent = new String(value, Charset.forName("UTF-8"));
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(bodyContent);
        return element.getAsJsonObject();
    }

    static JsonArray byteArrayToJsonArray(byte[] value) {
        String result = new String(value, Charset.forName("UTF-8"));
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(result);
        return jsonElement.getAsJsonArray();
    }

    static HashMap<String, String> readHeaderFromStream(InputStream input) throws IOException {
        HashMap<String, String> header = new HashMap<>();
        char c;
        StringBuilder builder = new StringBuilder();
        // here is a blocking code "input.read()"
        while ((c = (char) input.read()) != MessageHelper.HEADER_END_SEPARATOR) {
            if (c == MessageHelper.HEADER_LINE_SEPARATOR) {
                String line = builder.toString();
                line = line.replaceAll(" ", "");
                String[] sections = line.split(String.valueOf(MessageHelper.HEADER_PAIR_SEPARATOR));
                header.put(sections[0], sections[1]);
                builder = new StringBuilder();  // clear builder
            } else {
                builder.append(c);
            }
        }
        return header;
    }

    static byte[] readBodyFromStream(InputStream input, int bytesToRead) throws IOException {
        int bytesRead = 0;
        byte[] body = new byte[bytesToRead];
        while (bytesRead < bytesToRead) {
            int result = input.read(body, bytesRead, bytesToRead - bytesRead);
            if (result == -1) break; // end of stream
            bytesRead += result;
        }
        return body;
    }

    static byte[] createPacket(HashMap<String, String> header, byte[] content) {
        StringBuilder builder = new StringBuilder();
        Set set = header.entrySet();// Get a set of the entries
        Iterator i = set.iterator();// Get an iterator
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            builder.append(me.getKey().toString());
            builder.append(MessageHelper.HEADER_PAIR_SEPARATOR);
            builder.append(me.getValue().toString());
            builder.append(MessageHelper.HEADER_LINE_SEPARATOR);
        }
        builder.append(MessageHelper.HEADER_END_SEPARATOR);
        byte[] headerBytes = builder.toString().getBytes(Charset.forName("UTF-8"));
        byte[] packet = new byte[headerBytes.length + content.length];
        System.arraycopy(headerBytes, 0, packet, 0, headerBytes.length);
        System.arraycopy(content, 0, packet, headerBytes.length, content.length);
        return packet;
    }
}
