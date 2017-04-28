package ir.logicbase.mojmessenger.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Mahdi on 8/9/2017.
 * singleton with thread-safety
 * Define a Service that returns an IBinder for the
 * sync adapter class, allowing the sync adapter framework to call
 * onPerformSync().
 */

public class DatabaseSyncService extends Service{

    // Storage for an instance of the sync adapter
    private static DatabaseSyncAdapter syncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object lock = new Object();

    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (lock) {
            if (syncAdapter == null) {
                syncAdapter = new DatabaseSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return syncAdapter.getSyncAdapterBinder();
    }
}
