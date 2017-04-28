package ir.logicbase.mojmessenger.util;

import android.app.Application;

/**
 * Created by Mahdi on 5/3/2017.
 * Initialize any application configurations here
 * this class will be called at application start-up
 */

public class ApplicationLoader extends Application {

    private static ApplicationLoader instance;  // enable global access to context
    public static ApplicationLoader getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(new AppLifecycleTracker());
    }
}
