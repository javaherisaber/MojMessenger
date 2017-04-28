package ir.logicbase.mojmessenger.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by Mahdi on 7/24/2017.
 * synchronize local database with the server
 */

public class DatabaseSyncAdapter extends AbstractThreadedSyncAdapter {

    private ContentResolver resolver;  // interact with content provider

    public DatabaseSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        resolver = context.getContentResolver();
    }

    /**
     * This form of the constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public DatabaseSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        resolver = context.getContentResolver();
    }

    /**
     * Does the actual data transfer
     * Runs on a background thread
     *
     * @param account since we are using SharedPreferences for accessing user token, ignore this
     * @param extras a bundle of flags used by event that triggered sync adapter
     * @param authority used by content provider
     * @param provider similar to content resolver, since we have resolver just ignore this
     * @param syncResult A SyncResult object that you use to send information to the sync adapter framework.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

    }
}
