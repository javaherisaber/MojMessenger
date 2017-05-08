package socket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Mahdi on 8/31/2017.
 * Send messages to the client
 */
class MessageSender {

    private final OutputStream out;
    private LinkedList<byte[]> outboxQueue;
    private Thread worker;
    private Runnable job = new Runnable() {
        @Override
        public void run() {
            try {
                while (outboxQueue.size() > 0) {
                    byte[] packet = outboxQueue.getFirst();
                    out.write(packet);
                    out.flush();
                    outboxQueue.remove(packet);
                }
            } catch (IOException e) {
                // do nothing , just let the thread die
            }
        }
    };

    MessageSender(OutputStream out) {
        this.out = out;
        this.outboxQueue = new LinkedList<>();
    }

    synchronized void add(byte[] packet) {
        this.outboxQueue.addLast(packet);
        // check if worker busy
        if ((worker != null && !worker.isAlive()) || worker == null) {
            worker = new Thread(job);
            worker.start();
        }
    }
}
