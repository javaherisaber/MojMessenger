package ir.logicbase.mojmessenger.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.adapter.AdapterConversationList;
import ir.logicbase.mojmessenger.bundle.BundleMessage;
import ir.logicbase.mojmessenger.database.DatabaseGateway;
import ir.logicbase.mojmessenger.socket.IncomingGateway;
import ir.logicbase.mojmessenger.socket.OutgoingGateway;
import ir.logicbase.mojmessenger.util.PermissionsRequest;
import ir.logicbase.mojmessenger.util.PrefManager;
import ir.logicbase.mojmessenger.util.Time;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * Private chat conversation screen
 */
public class ActivityConversation extends AppCompatActivity implements View.OnClickListener,
        IncomingGateway.TextMessageListener {

    public static final String INTENT_KEY_CONTACT_ID = "ContactId";
    public static final String INTENT_KEY_CONVERSATION_TITLE = "ConversationTitle";
    public static final String INTENT_KEY_CONVERSATION_PIC = "ConversationPic";
    public static final String INTENT_KEY_USER_LAST_SEEN = "UserLastSeen";
    public static final String INTENT_KEY_USER_PHONE = "UserPhone";

    private EditText edTextMessage;
    private TextView txtEmpty;
    private AdapterConversationList adapter;
    private RecyclerView recView;
    private String title, pic, lastSeen, phone;
    private int contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // get intent
        this.contactId = getIntent().getIntExtra(INTENT_KEY_CONTACT_ID, -1);
        this.title = getIntent().getStringExtra(INTENT_KEY_CONVERSATION_TITLE);
        this.pic = getIntent().getStringExtra(INTENT_KEY_CONVERSATION_PIC);
        this.lastSeen = getIntent().getStringExtra(INTENT_KEY_USER_LAST_SEEN);
        this.phone = getIntent().getStringExtra(INTENT_KEY_USER_PHONE);

        // init views
        this.recView = (RecyclerView) findViewById(R.id.recView_conversation);
        this.edTextMessage = (EditText) findViewById(R.id.edText_message);
        txtEmpty = (TextView) findViewById(R.id.txtView_conversation_list_empty);
        ImageView btnSend = (ImageView) findViewById(R.id.btn_send_message);

        // set background
        PrefManager pref = new PrefManager(getApplicationContext());
        String imagePath = pref.getSettingsPrefChatBackgroundPic();
        if (imagePath == null || imagePath.equals("null")) {
            getWindow().setBackgroundDrawableResource(R.drawable.conversation_bg);
        } else {
            if (PermissionsRequest.checkWriteExternalStoragePermission(getApplicationContext(), this)) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                getWindow().setBackgroundDrawable(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
            }
        }

        // init toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_conversation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CircleImageView profile_pic = (CircleImageView) toolbar.findViewById(R.id.imView_conversation_pic);
        TextView txtViewTitle = (TextView) toolbar.findViewById(R.id.txtView_conversation_title);
        TextView txtViewLastSeen = (TextView) toolbar.findViewById(R.id.txtView_conversation_last_seen);
        RelativeLayout toolbarArea = (RelativeLayout) toolbar.findViewById(R.id.toolbar_conversation_area);
        txtViewTitle.setText(this.title);
        Picasso.with(this)
                .load(this.pic)
                .placeholder(R.drawable.default_person_pic)
                .error(R.drawable.default_person_pic)
                .into(profile_pic);
        String userLastSeen = "آخرین بازدید : " + Time.getLastSeenLabel(this.lastSeen);
        txtViewLastSeen.setText(userLastSeen);

        // change fonts
        Typeface typeface = TypefaceManager.get(this, getString(R.string.font_iran_sans));
        txtViewTitle.setTypeface(typeface);
        txtViewLastSeen.setTypeface(typeface);
        txtEmpty.setTypeface(typeface);

        // init conversation recyclerView
        // FIXME: 12/5/2017 use paging and load one page in here
        DatabaseGateway dbGateway = new DatabaseGateway(getApplicationContext());
        ArrayList<BundleMessage> messages = dbGateway.getAllMessages(this.contactId);
        dbGateway.closeDatabase();
        this.adapter = new AdapterConversationList(txtEmpty, messages);
        recView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(false);
        layoutManager.setStackFromEnd(true);
        recView.setLayoutManager(layoutManager);

        // set listeners
        IncomingGateway.getInstance().setTextMessageListener(this);
        toolbarArea.setOnClickListener(this);
        btnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_message:
                onSendMessageClick();
                break;
            case R.id.toolbar_conversation_area:
                onToolbarAreaClick();
                break;
        }
    }

    private void onToolbarAreaClick() {
        Intent intent = new Intent(ActivityConversation.this, ActivityProfile.class);
        intent.putExtra(ActivityProfile.INTENT_KEY_CONTACT_ID, contactId);
        intent.putExtra(ActivityProfile.INTENT_KEY_NAME, title);
        intent.putExtra(ActivityProfile.INTENT_KEY_PIC, pic);
        intent.putExtra(ActivityProfile.INTENT_KEY_LAST_SEEN, lastSeen);
        intent.putExtra(ActivityProfile.INTENT_KEY_PHONE, phone);
        startActivity(intent);
    }

    private void onSendMessageClick() {
        String message = edTextMessage.getText().toString();
        if (!message.isEmpty()) {
            BundleMessage bundleMessage = new BundleMessage(true, Time.getClock(), message);
            DatabaseGateway dbGateway = new DatabaseGateway(getApplicationContext());
            int id = dbGateway.insertMessage(contactId, bundleMessage);
            bundleMessage.setId(id);
            addItemToAdapter(bundleMessage);
            edTextMessage.getText().clear();
            PrefManager pref = new PrefManager(getApplicationContext());
            OutgoingGateway.SendTextMessage(pref.getPhoneNumber(), this.phone, message);
            dbGateway.closeDatabase();
        }
    }

    private void addItemToAdapter(BundleMessage message) {
        adapter.addItem(message);
        adapter.notifyDataSetChanged();
        int position = adapter.getItemCount() - 1;
        recView.smoothScrollToPosition(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search: {
                // do your search stuff
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                return true;
            }
            // case blocks for other MenuItems (if any)
        }
        return false;
    }

    @Override
    public void onTextMessageArrive(final String text) {
        runOnUiThread(() -> {
            // FIXME: 12/5/2017 change the protocol, the new arriving message must have msgContent, timestamp, phone and serverMsgId
            int serverMsgId = -1; // change this to real one
            BundleMessage bundleMessage = new BundleMessage(false, Time.getClock(), text);
            DatabaseGateway dbGateway = new DatabaseGateway(getApplicationContext());
            int id = dbGateway.insertMessage(serverMsgId, contactId, bundleMessage);
            bundleMessage.setId(id);
            bundleMessage.setServerMsgId(serverMsgId);
            addItemToAdapter(bundleMessage);
            dbGateway.closeDatabase();
        });
    }

    @Override
    public void onAckArrive() {

    }
}
