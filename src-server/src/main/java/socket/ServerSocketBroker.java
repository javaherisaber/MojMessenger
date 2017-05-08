package socket;

import entity.ClientEntity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Mahdi on 7/15/2017.
 * a bridge for client's socket
 */
public class ServerSocketBroker {

    // this port is specified for messages only
    private static final int PORT_NUMBER = 6700;
    private static final int HEART_BEAT_TIME_OUT = 30 * 1000;
    // HashMap<phone_number, ClientEntity>
    static final HashMap<String, ClientEntity> connectedClients = new HashMap<>();

    public static void main(String[] args){
        System.out.println("Message Server Started !");
        ExecutorService pool = Executors.newFixedThreadPool(1000);
        try (ServerSocket server = new ServerSocket(PORT_NUMBER)) {
            new UploadSocketBroker().work();
            while (true) {
                try {
                    Socket connection = server.accept();
                    connection.setSoTimeout(HEART_BEAT_TIME_OUT);
                    Callable<Void> task = new ClientTask(connection);
                    pool.submit(task);
                } catch (IOException ex) {}
            }
        } catch (IOException ex) {
            System.err.println("Couldn't start server");
        }
    }


}
