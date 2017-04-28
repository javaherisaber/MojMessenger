package ir.logicbase.mojmessenger.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.contact.DialogNewContact;
import ir.logicbase.mojmessenger.database.SyncDatabase;
import ir.logicbase.mojmessenger.settings.ActivitySettings;
import ir.logicbase.mojmessenger.socket.ConnectionHandler;
import ir.logicbase.mojmessenger.util.Connectivity;
import ir.logicbase.mojmessenger.util.PrefManager;
import ir.logicbase.mojmessenger.verification.ActivitySmsVerification;

/**
 * Top level screen of our app
 * powered by fragments
 */
public class ActivityHome extends AppCompatActivity implements ConnectionHandler.ConnectionListener,
        SyncDatabase.SyncDatabaseListener {

    private TextView activityTitle;
    private BottomNavigationView navigation;
    private ViewPager homePager;
    private MenuItem prevMenuItem;

    private BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectionHandler connection = ConnectionHandler.getInstance();
            if (!Connectivity.isConnected(context)) {
                connection.closeConnection();
            } else {
                connection.startConnection();
            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_private:
                homePager.setCurrentItem(0);
                return true;
            case R.id.navigation_contacts:
                homePager.setCurrentItem(1);
                return true;
        }
        return false;
    };

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (prevMenuItem != null) {
                prevMenuItem.setChecked(false);  // unCheck previous item
            } else {
                // no previous item yet
                navigation.getMenu().getItem(0).setChecked(false);
            }

            navigation.getMenu().getItem(position).setChecked(true);
            prevMenuItem = navigation.getMenu().getItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefManager pref = new PrefManager(this);
        if (!pref.getIsRegistered()) {
            Intent intent = new Intent(this, ActivitySmsVerification.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_home);

            navigation = findViewById(R.id.btnNavigation_home);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            homePager = findViewById(R.id.homePager);
            AdapterHomePager adapterHomePager = new AdapterHomePager(getSupportFragmentManager());

            homePager.setAdapter(adapterHomePager);
            homePager.setCurrentItem(0);
            homePager.setOffscreenPageLimit(2);
            homePager.addOnPageChangeListener(pageChangeListener);


            Toolbar toolbar = findViewById(R.id.toolbar_home);
            setSupportActionBar(toolbar);
            ImageView imViewSettings = toolbar.findViewById(R.id.imView_home_settings);
            imViewSettings.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityHome.this, ActivitySettings.class);
                startActivity(intent);
            });

            activityTitle = toolbar.findViewById(R.id.txtView_title);
            activityTitle.setText("در انتظار برای اینترنت...");

            // send go online when coming from smsVerification activity
            ConnectionHandler connection = ConnectionHandler.getInstance();
            connection.setConnectionListener(this);
            if (!connection.wantOnline() && !connection.isOnline() && connection.isSocketConnected()) {
                connection.setWantOnline(true);  // go online
                connection.interrupt();
                Log.d("MojMessengerNetwork", "ActivityHome want goOnline");
            }

            // sync when coming from smsVerification activity
            SyncDatabase.getInstance().setSyncDatabaseListener(this);
            SyncDatabase.getInstance().startSyncing();

            FloatingActionButton fab = findViewById(R.id.fab_home);
            fab.setOnClickListener(view -> fabAction());
        }
    }

    private void fabAction() {
        int item = navigation.getSelectedItemId();
        switch (item) {
            case R.id.navigation_private:
                navigation.setSelectedItemId(R.id.navigation_contacts);
                break;
            case R.id.navigation_contacts:
                new DialogNewContact().showDialog(this, this);
                break;
        }
    }

    @Override
    public void waitingForNetwork() {
        runOnUiThread(() -> activityTitle.setText("در انتظار برای اینترنت..."));
    }

    @Override
    public void connecting() {
        runOnUiThread(() -> activityTitle.setText("در حال اتصال..."));
    }

    @Override
    public void onUserGoesOnline() {
        runOnUiThread(() -> activityTitle.setText("آنلاین"));
    }

    @Override
    public void disconnected() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(connectivityChangeReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(this.connectivityChangeReceiver);
    }

    @Override
    public void syncing() {
        runOnUiThread(() -> activityTitle.setText("در حال بروز رسانی"));
    }

    @Override
    public void syncCompleted() {

    }
}
