package ir.logicbase.mojmessenger.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * Created by Mahdi on 7/21/2017.
 * Send messages to the server
 */

class MessageSender {

    private static final LinkedList<PacketEntity> queue = new LinkedList<>();
    private final OutputStream out;
    private Thread worker;
    private Runnable job = new Runnable() {
        @Override
        public void run() {
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    while (queue.size() > 0) {
                        PacketEntity entity = queue.getFirst();
                        byte[] packet = entity.createPacket();
                        out.write(packet);
                        out.flush();
                        queue.remove(entity);
                    }
                }
            } catch (IOException e) {
                // do nothing , just let the thread die
            }
        }
    };

    MessageSender(OutputStream out) {
        this.out = out;
    }

    synchronized void work() {
        if ((worker != null && !worker.isAlive()) || worker == null) {
            worker = new Thread(job, this.getClass().getSimpleName());
            worker.start();
        }
    }

    static void addToQueue(PacketEntity entity) {
        queue.addLast(entity);
    }
}
