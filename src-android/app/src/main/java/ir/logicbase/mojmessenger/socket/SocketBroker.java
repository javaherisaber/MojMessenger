package ir.logicbase.mojmessenger.socket;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;

/**
 * Created by Mahdi on 7/22/2017.
 * Singleton -> powered by "initialization on demand" in order to ensure thread-safety and performance
 * a bridge to access server
 */

final class SocketBroker {

    private Socket socket;
    private MessageSender sender;
    private Thread worker;

    private static final int HEART_BEAT_TIME_OUT = 30 * 1000;
    private static final String SERVER_ADDRESS = "www.logicbase.ir";
    private static final int SERVER_PORT = 6700;

    private SocketListener listener;

    interface SocketListener {
        void socketConnected();

        void socketDisconnected();
    }

    /*
     * prevent from instantiation
     */
    private SocketBroker() {
    }

    /*
     * powered by "initialization on demand" idiom
     */
    private static class lazyHolder {
        static final SocketBroker instance = new SocketBroker();
    }

    /**
     * @return the same instance of class every time
     */
    public static SocketBroker getInstance() {
        return lazyHolder.instance;
    }

    boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    void setListener(SocketListener listener) {
        this.listener = listener;
    }

    synchronized void connect() {
        if ((worker != null && !worker.isAlive()) || worker == null) {
            worker = new Thread(job, this.getClass().getSimpleName());
            worker.start();
        }
    }

    /**
     * by closing a socket all of its resources will be free, including Input and Output streams
     * MessageReceiver and MessageSender get's I/O exception and then they will be finished
     */
    synchronized void close() {
        if (isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    void sendMessage(PacketEntity message) {
        Log.d("MojMessengerNetwork", message.getHeaders().get(MessageHelper.HEADER_KEY_METHOD));
        MessageSender.addToQueue(message);
        if (sender != null) {
            sender.work();
        }
    }

    private Runnable job = new Runnable() {
        @Override
        public void run() {
            try {
                socket = new Socket();
                SocketAddress address = new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT);
                socket.connect(address);
                socket.setSoTimeout(HEART_BEAT_TIME_OUT);
                sender = new MessageSender(socket.getOutputStream());
                if (listener != null) {
                    listener.socketConnected();
                }
                InputStream input = socket.getInputStream();
                //noinspection InfiniteLoopStatement
                while (true) {
                    // read new message from stream
                    HashMap<String, String> header = MessageHelper.readHeaderFromStream(input);
                    int bytesToRead = Integer.parseInt(header.get(MessageHelper.HEADER_KEY_CONTENT_LENGTH));
                    byte[] body = MessageHelper.readBodyFromStream(input, bytesToRead);

                    // inform gateway
                    PacketEntity message = new PacketEntity(header, body);
                    IncomingGateway gateway = IncomingGateway.getInstance();
                    gateway.onMessageArrive(message);
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.socketDisconnected();
                }
            } finally {
                close();
            }
        }
    };
}
