package ir.logicbase.mojmessenger.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This service will be run whenever a new account is being created by this account
 */
public class AccountAuthenticatorService extends Service {

    private AccountAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        authenticator = new AccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}