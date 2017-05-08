package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Mahdi on 12/10/2017.
 * entry point to accept upload tasks from clients
 */
class UploadSocketBroker {

    private static final int PORT_NUMBER = 6701;
    private Thread worker;

    private Runnable job = () -> {
        System.out.println("Upload Server Started !");
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try (ServerSocket server = new ServerSocket(PORT_NUMBER)) {
            while (true) {
                try {
                    Socket connection = server.accept();
                    Callable<Void> task = new UploadServerTask(connection);
                    pool.submit(task);
                } catch (IOException ex) {
                    // do nothing
                }
            }
        } catch (IOException ex) {
            System.err.println("Couldn't start upload server");
        }
    };

    void work() {
        if ((worker != null && !worker.isAlive()) || worker == null) {
            worker = new Thread(job, this.getClass().getSimpleName());
            worker.start();
        }
    }
}
