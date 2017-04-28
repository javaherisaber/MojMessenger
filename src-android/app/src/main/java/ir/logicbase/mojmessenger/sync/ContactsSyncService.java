package ir.logicbase.mojmessenger.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Mahdi on 8/5/2017.
 * used as a counterpart of syncAdapter
 * acts like a singleton with thread-safety feature
 */

public class ContactsSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static ContactsSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null)
                sSyncAdapter = new ContactsSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
