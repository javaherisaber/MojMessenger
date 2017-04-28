package ir.logicbase.mojmessenger.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by Mahdi on 8/5/2017.
 * Sync contacts data using a custom account type as authenticator
 */

public class ContactsSyncAdapter extends AbstractThreadedSyncAdapter{

    public ContactsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        // nothing special yet, maybe later i will implement this to sync contacts with server
    }
}
