package ir.logicbase.mojmessenger.sync;

import ir.logicbase.mojmessenger.socket.OutgoingGateway;
import ir.logicbase.mojmessenger.util.ApplicationLoader;
import ir.logicbase.mojmessenger.util.PrefManager;

/**
 * Created by Mahdi on 12/6/2017.
 * watch activity lifecycle callbacks and sync online status
 */

public class OnlineStatusObserver {

    private static boolean isOnline = false;
    private static boolean wantOnline = false;
    private boolean isOnClose = false;
    private static int TIME_OUT = 10 * 1000;

    private Thread worker;
    private Runnable job = () -> {
        while (!isOnClose) {
            if (!isOnline && wantOnline) {
                // send go online
                PrefManager pref = new PrefManager(ApplicationLoader.getInstance());
                OutgoingGateway.SendGoOnline(pref.getPhoneNumber());
            } else if (isOnline && !wantOnline) {
                // send go offline
                PrefManager pref = new PrefManager(ApplicationLoader.getInstance());
                OutgoingGateway.SendGoOffline(pref.getPhoneNumber());
            }
            try {
                Thread.sleep(TIME_OUT);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    };

    public void start() {
        if ((worker != null && !worker.isAlive()) || worker == null) {
            worker = new Thread(job, this.getClass().getName());
            worker.start();
        }
    }

    public void stop() {
        this.isOnClose = true;
    }

    public static void setWantOnline(boolean value) {
        wantOnline = value;
    }

    public static void setIsOnline(boolean value) {
        isOnline = value;
    }
}
