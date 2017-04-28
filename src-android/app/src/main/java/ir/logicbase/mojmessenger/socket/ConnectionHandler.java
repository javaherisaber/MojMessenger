package ir.logicbase.mojmessenger.socket;

import android.util.Log;

import ir.logicbase.mojmessenger.database.SyncDatabase;
import ir.logicbase.mojmessenger.util.ApplicationLoader;
import ir.logicbase.mojmessenger.util.PrefManager;

/**
 * Created by Mahdi on 7/22/2017.
 * Singleton -> powered by "initialization on demand" in order to ensure thread-safety and performance
 * this thread observe's connection and keep's connection alive
 */

public final class ConnectionHandler implements SocketBroker.SocketListener, IncomingGateway.GoOnlineListener {

    private static final long THREAD_TIME_OUT = 10 * 1000;
    private boolean isOnline = false;
    private boolean wantOnline = false;
    private boolean isOnClose = false;

    private ConnectionListener connectionListener;
    private Thread worker;

    @Override
    public void socketConnected() {
        if (connectionListener != null) {
            connectionListener.connecting();
        }
        // sync when connection reset
        SyncDatabase.getInstance().startSyncing();
    }

    @Override
    public void socketDisconnected() {
        setIsOnline(false);
        setWantOnline(false);
        if (connectionListener != null) {
            connectionListener.disconnected();
        }
    }

    @Override
    public void onGoingOnline() {
        setIsOnline(true);
        if (connectionListener != null) {
            connectionListener.onUserGoesOnline();
        }
    }

    @Override
    public void onGoingOffline() {
        setIsOnline(false);
        setWantOnline(false);
        if (connectionListener != null) {
            connectionListener.disconnected();
        }
    }

    public interface ConnectionListener {
        void waitingForNetwork();

        void connecting();

        void onUserGoesOnline();

        void disconnected();
    }

    /*
     * prevent from instantiation
     */
    private ConnectionHandler() {
        SocketBroker broker = SocketBroker.getInstance();
        broker.setListener(this);
        IncomingGateway gateway = IncomingGateway.getInstance();
        gateway.setGoOnlineListener(this);
    }

    /*
     * powered by "initialization on demand" idiom
     */
    private static class lazyHolder {
        static final ConnectionHandler instance = new ConnectionHandler();
    }

    /**
     * @return the same instance of class every time
     */
    public static ConnectionHandler getInstance() {
        return lazyHolder.instance;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public void startConnection() {
        SocketBroker broker = SocketBroker.getInstance();
        broker.connect();
        isOnClose = false;
        if ((worker != null && !worker.isAlive()) || worker == null) {
            worker = new Thread(job, this.getClass().getSimpleName());
            worker.start();
        }
    }

    public void closeConnection() {
        SocketBroker broker = SocketBroker.getInstance();
        broker.close();
        setIsOnline(false);
        setWantOnline(false);
    }

    public void stopConnectionHandler() {
        SocketBroker broker = SocketBroker.getInstance();
        broker.close();
        this.isOnClose = true;
    }

    public void interrupt() {
        if (worker != null) {
            worker.interrupt();
        }
    }

    public void setWantOnline(boolean value) {
        wantOnline = value;
    }

    public boolean wantOnline() {
        return wantOnline;
    }

    public boolean isSocketConnected() {
        SocketBroker broker = SocketBroker.getInstance();
        return broker.isConnected();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean value) {
        isOnline = value;
    }

    private Runnable job = new Runnable() {
        @Override
        public void run() {
            while (!isOnClose) {
                if (!isSocketConnected()) {
                    if (connectionListener != null) {
                        connectionListener.waitingForNetwork();
                    }
                    startConnection();
                } else {
                    Log.d("MojMessengerNetwork", "isOnline = " + isOnline + " wantOnline = " + wantOnline);
                    if (!isOnline && wantOnline) {
                        // send go online
                        PrefManager pref = new PrefManager(ApplicationLoader.getInstance());
                        OutgoingGateway.sendGoOnline(pref.getPhoneNumber());
                    } else if (isOnline && !wantOnline) {
                        // send go offline
                        PrefManager pref = new PrefManager(ApplicationLoader.getInstance());
                        OutgoingGateway.sendGoOffline(pref.getPhoneNumber());
                    }
                    // send a heartbeat packet to keep connection alive
                    OutgoingGateway.sendHeartbeat();
                }
                try {
                    Thread.sleep(THREAD_TIME_OUT);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
    };

}
