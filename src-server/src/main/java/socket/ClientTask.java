package socket;

import database.DatabaseGateway;
import entity.ClientEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by Mahdi on 7/15/2017.
 * receive and process a client socket
 */
public class ClientTask implements Callable<Void> {

    private Socket socket;
    private MessageSender sender;

    public ClientTask(Socket c) {
        this.socket = c;
    }

    @Override
    public Void call() {
        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            sender = new MessageSender(output);
            while (true) {
                // read new message from stream
                HashMap<String, String> header = MessageHelper.readHeaderFromStream(input);
                int bytesToRead = Integer.parseInt(header.get(MessageHelper.HEADER_KEY_CONTENT_LENGTH));
                byte[] body = MessageHelper.readBodyFromStream(input, bytesToRead);

                System.out.println("incoming message : " + header.get("Method"));
                String data = new String(body, Charset.forName("UTF-8"));
                System.out.println(data);

                ActionCenter.processRequest(header, body, this);
            }
        } catch (IOException e) {
            // socket is closed or other related network error
            return null;  // ending this thread
        } finally {
            try {
                for (Map.Entry<String, ClientEntity> entry : ServerSocketBroker.connectedClients.entrySet()) {
                    ClientEntity entity = entry.getValue();
                    if (entity.getTask() == this) {
                        String key = entry.getKey();  // user phone
                        ServerSocketBroker.connectedClients.remove(key);
                        System.out.println("Closing client : " + key);
                        DatabaseGateway.updateUserLastSeen(key);
                        break;
                    }
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addToOutbox(byte[] packet) {
        sender.add(packet);
    }
}
