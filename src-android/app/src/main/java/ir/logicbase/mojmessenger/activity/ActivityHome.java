package ir.logicbase.mojmessenger.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.contact.DialogNewContact;
import ir.logicbase.mojmessenger.activity.fragment.FragmentHomeChatList;
import ir.logicbase.mojmessenger.activity.fragment.FragmentHomeContactList;
import ir.logicbase.mojmessenger.socket.ConnectionHandler;
import ir.logicbase.mojmessenger.util.Connectivity;
import ir.logicbase.mojmessenger.util.PrefManager;

/**
 * Top level screen of our app
 * powered by fragments
 */
public class ActivityHome extends AppCompatActivity implements ConnectionHandler.ConnectionListener {

    private TextView activityTitle;
    private BottomNavigationView navigation;

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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_private:
                    swapFragment(new FragmentHomeChatList());
                    return true;
                case R.id.navigation_contacts:
                    swapFragment(new FragmentHomeContactList());
                    return true;
            }
            return false;
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

            swapFragment(new FragmentHomeChatList());

            navigation = (BottomNavigationView) findViewById(R.id.btnNavigation_home);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_home);
            setSupportActionBar(toolbar);
            ImageView imViewSearch = (ImageView) toolbar.findViewById(R.id.imView_home_search);
            ImageView imViewSettings = (ImageView) toolbar.findViewById(R.id.imView_home_settings);
            imViewSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ActivityHome.this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
                }
            });
            imViewSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityHome.this, ActivitySettings.class);
                    startActivity(intent);
                }
            });

            activityTitle = (TextView) toolbar.findViewById(R.id.txtView_title);
            activityTitle.setText("آفلاین");
            ConnectionHandler connection = ConnectionHandler.getInstance();
            connection.setListener(this);
            connection.startConnection();

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_home);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fabAction();
                }
            });
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

    private void swapFragment(Fragment fragment) {
        /*
        Working with BackStack
        The back stack is the list of places that you’ve visited on the device.
        Each place is a transaction on the back stack.
        */
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();//call fragment manager from parent activity
        ft.replace(R.id.fragment_home_container, fragment);  // replace the fragment with a new one
        /*
        This allows the user to go back to a previous state of the fragment when they press the Back button
        the Parameter is a String name to label the transaction
        */
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);  // add animation to replacement process
        ft.commit(); // commit the transaction
    }

    @Override
    public void waitingForNetwork() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activityTitle.setText("در انتظار برای اینترنت...");
            }
        });
    }

    @Override
    public void connecting() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activityTitle.setText("در حال اتصال...");
            }
        });
    }

    @Override
    public void onUserGoesOnline() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activityTitle.setText("آنلاین");
            }
        });
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
}
