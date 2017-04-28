package ir.logicbase.mojmessenger.socket;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mahdi on 7/22/2017.
 * The message itself being sent by socket
 */

class PacketEntity {

    private HashMap<String, String> headers;
    private byte[] body;

    PacketEntity(HashMap<String, String> headers, byte[] body) {
        this.headers = headers;
        this.body = body;
    }

    HashMap<String, String> getHeaders() {
        return headers;
    }

    byte[] getBody() {
        return body;
    }

    /**
     * @return packet based on the protocol
     */
    byte[] createPacket() {
        StringBuilder builder = new StringBuilder();
        Set set = headers.entrySet();
        Iterator i = set.iterator();
        // header section
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            builder.append(me.getKey().toString());
            builder.append(MessageHelper.HEADER_PAIR_SEPARATOR);
            builder.append(me.getValue().toString());
            builder.append(MessageHelper.HEADER_LINE_SEPARATOR);
        }
        builder.append(MessageHelper.HEADER_END_SEPARATOR);
        byte[] headerBytes = builder.toString().getBytes(Charset.forName("UTF-8"));
        byte[] packet = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, packet, 0, headerBytes.length);
        System.arraycopy(body, 0, packet, headerBytes.length, body.length);
        return packet;
    }
}
