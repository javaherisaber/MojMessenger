package ir.logicbase.mojmessenger.util;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;

import ir.logicbase.mojmessenger.socket.ConnectionHandler;

/**
 * Created by Mahdi on 12/6/2017.
 * this callbacks will be called by entering any activity
 */

public class AppLifecycleTracker implements ActivityLifecycleCallbacks {

    private static int numStarted = 0;
    private static int numCreated = 0;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d("MojMessengerActivity", activity.getClass().getSimpleName() + " Created");
        if (numCreated == 0) {
            Log.d("MojMessengerActivity", "app process starting");
            ConnectionHandler connection = ConnectionHandler.getInstance();
            connection.startConnection();
        }
        numCreated++;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d("MojMessengerActivity", activity.getClass().getSimpleName() + " Started");
        if (numStarted == 0) {
            Log.d("MojMessengerActivity", "app went to foreground");
            PrefManager pref = new PrefManager(activity.getApplicationContext());
            if (pref.getIsRegistered()) {
                ConnectionHandler c = ConnectionHandler.getInstance();
                if (!c.wantOnline() && !c.isOnline() && c.isSocketConnected()) {
                    c.setWantOnline(true);  // go online
                    c.interrupt();
                    Log.d("MojMessengerNetwork", "LifecycleTracker want goOnline");
                }
            }
        }
        numStarted++;
        Log.d("MojMessengerActivity" , " numStarted=" + numStarted);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d("MojMessengerActivity", activity.getClass().getSimpleName() + " Resumed");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d("MojMessengerActivity", activity.getClass().getSimpleName() + " Paused");

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d("MojMessengerActivity", activity.getClass().getSimpleName() + " Stopped");
        numStarted--;
        if (numStarted == 0) {
            Log.d("MojMessengerActivity", "app went to background");
            PrefManager pref = new PrefManager(activity.getApplicationContext());
            if (pref.getIsRegistered()) {
                ConnectionHandler c = ConnectionHandler.getInstance();
                if (c.wantOnline() && c.isOnline()) {
                    c.setWantOnline(false);  // go offline
                    c.interrupt();
                }
            }
        }
        Log.d("MojMessengerActivity" , " numStarted=" + numStarted);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d("MojMessengerActivity", activity.getClass().getSimpleName() + " SaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d("MojMessengerActivity", activity.getClass().getSimpleName() + " Destroyed");
        numCreated--;
        if (numCreated == 0) {
            Log.d("MojMessengerActivity", "app process going to get killed!");
            ConnectionHandler.getInstance().stopConnectionHandler();
        }
    }

    public static boolean isAppInForeground() {
        return numStarted != 0;
    }

    public static boolean isAppProcessRunning() {
        return numCreated != 0;
    }
}
