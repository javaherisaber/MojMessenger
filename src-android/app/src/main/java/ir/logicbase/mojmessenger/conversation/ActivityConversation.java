package ir.logicbase.mojmessenger.conversation;

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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.database.message.MessageEntity;
import ir.logicbase.mojmessenger.profile.ActivityProfile;
import ir.logicbase.mojmessenger.util.PermissionsRequest;
import ir.logicbase.mojmessenger.util.PrefManager;
import ir.logicbase.mojmessenger.util.Time;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * Private chat conversation screen
 */
public class ActivityConversation extends AppCompatActivity implements View.OnClickListener, ConversationView {

    // intent keys
    public static final String INTENT_KEY_CONTACT_ID = "ContactId";
    public static final String INTENT_KEY_CONTACT_NAME = "ConversationTitle";
    public static final String INTENT_KEY_CONTACT_PHONE = "UserPhone";
    public static final String INTENT_KEY_CONTACT_PIC = "ConversationPic";
    public static final String INTENT_KEY_CONTACT_LAST_SEEN = "UserLastSeen";
    public static final String INTENT_KEY_CONTACT_IS_ONLINE = "isOnline";

    private ConversationPresenter presenter;

    private EditText edTextMessage;
    private ProgressBar progressBar;
    private AdapterConversationList adapter;
    private RecyclerView recycler;
    private ImageView imgProfilePic;
    private TextView txtLastSeen;

    private ContactEntity contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // get intent
        int contactId = getIntent().getIntExtra(INTENT_KEY_CONTACT_ID, 0);
        String name = getIntent().getStringExtra(INTENT_KEY_CONTACT_NAME);
        String pic = getIntent().getStringExtra(INTENT_KEY_CONTACT_PIC);
        String lastSeen = getIntent().getStringExtra(INTENT_KEY_CONTACT_LAST_SEEN);
        String phone = getIntent().getStringExtra(INTENT_KEY_CONTACT_PHONE);
        boolean isOnline = getIntent().getBooleanExtra(INTENT_KEY_CONTACT_IS_ONLINE, false);
        contact = new ContactEntity(contactId, name, phone, pic, lastSeen, isOnline);

        // init views
        this.recycler = findViewById(R.id.recView_conversation);
        this.edTextMessage = findViewById(R.id.edText_message);
        TextView txtEmpty = findViewById(R.id.txtView_conversation_list_empty);
        ImageView btnSend = findViewById(R.id.btn_send_message);
        progressBar = findViewById(R.id.progress_conversation);

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
        Toolbar toolbar = findViewById(R.id.toolbar_conversation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imgProfilePic = toolbar.findViewById(R.id.imView_conversation_pic);
        TextView txtName = toolbar.findViewById(R.id.txtView_conversation_title);
        txtLastSeen = toolbar.findViewById(R.id.txtView_conversation_last_seen);
        RelativeLayout toolbarArea = toolbar.findViewById(R.id.toolbar_conversation_area);
        txtName.setText(contact.getName());
        Picasso.with(this)
                .load(contact.getPic())
                .placeholder(R.drawable.default_person_pic)
                .error(R.drawable.default_person_pic)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(imgProfilePic);
        if (contact.isOnline()) {
            txtLastSeen.setText("آنلاین");
        } else {
            String userLastSeen = "آخرین بازدید : " + Time.getLastSeenLabel(contact.getLastSeen());
            txtLastSeen.setText(userLastSeen);
        }

        // change fonts
        Typeface typeface = TypefaceManager.get(this, getString(R.string.font_iran_sans));
        txtName.setTypeface(typeface);
        txtLastSeen.setTypeface(typeface);
        txtEmpty.setTypeface(typeface);

        // init adapter
        // FIXME: 12/5/2017 use paging and load one page in here
        this.adapter = new AdapterConversationList(txtEmpty);
        recycler.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(false);
        layoutManager.setStackFromEnd(true);
        recycler.setLayoutManager(layoutManager);

        // set listeners
        toolbarArea.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        // start presenter
        presenter = new ConversationPresenter(this, this);
        presenter.initiateMessages(contactId);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        progressBar.setVisibility(View.GONE);
        presenter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.startListening(this,this);
        presenter.syncProfile(contact.getPhone());
        presenter.initiateMessages(contact.getId());
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
        intent.putExtra(ActivityProfile.INTENT_KEY_CONTACT_ID, contact.getId());
        intent.putExtra(ActivityProfile.INTENT_KEY_NAME, contact.getName());
        intent.putExtra(ActivityProfile.INTENT_KEY_PIC, contact.getPic());
        intent.putExtra(ActivityProfile.INTENT_KEY_LAST_SEEN, contact.getLastSeen());
        intent.putExtra(ActivityProfile.INTENT_KEY_PHONE, contact.getPhone());
        intent.putExtra(ActivityProfile.INTENT_KEY_IS_ONLINE, contact.isOnline());
        startActivity(intent);
    }

    private void onSendMessageClick() {
        String message = edTextMessage.getText().toString();
        if (!message.isEmpty()) {
            String sender = new PrefManager(this).getPhoneNumber();
            String recipient = contact.getPhone();
            presenter.sendMessage(sender, recipient, new MessageEntity(contact.getId(), message));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            // case blocks for other MenuItems (if any)
        }
        return false;
    }

    @Override
    public void initiateConversationList(int contactId, List<MessageEntity> messages) {
        if (contact.getId() == contactId) {
            progressBar.setVisibility(View.GONE);
            adapter.swapDataSource(messages);
            presenter.seenMessages(this, contact.getId(), contact.getPhone());
        }
    }

    @Override
    public void insertNewMessage(MessageEntity message) {
        if (contact.getId() == message.getFkContactId()) {
            adapter.addItem(message, recycler);
            edTextMessage.getText().clear();
        }
    }

    @Override
    public void updateContact(ContactEntity contact) {
        if (this.contact.getId() == contact.getId()) {
            if (!this.contact.getPic().equals(contact.getPic())) {
                Picasso.with(this)
                        .load(contact.getPic())
                        .placeholder(R.drawable.default_person_pic)
                        .error(R.drawable.default_person_pic)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(imgProfilePic);
            }
            if (!this.contact.getLastSeen().equals(contact.getLastSeen()) || contact.isOnline()) {
                if (contact.isOnline()) {
                    txtLastSeen.setText("آنلاین");
                } else {
                    String userLastSeen = "آخرین بازدید : " + Time.getLastSeenLabel(contact.getLastSeen());
                    txtLastSeen.setText(userLastSeen);
                }
            }
            this.contact = contact;
        }
    }

    @Override
    public void deliverMessage(MessageEntity message) {
        if (this.contact.getId() == message.getFkContactId()) {
            String sender = this.contact.getPhone();
            String recipient = new PrefManager(this).getPhoneNumber();
            presenter.sendSeenFlag(sender, recipient, message.getServerMessageId());
            adapter.addItem(message, recycler);
        }
    }

    @Override
    public void seenAck(MessageEntity message) {
        if (this.contact.getId() == message.getFkContactId()) {
            adapter.updateItem(message);
        }
    }

    @Override
    public void textMessageAck(MessageEntity message) {
        if (this.contact.getId() == message.getFkContactId()) {
            adapter.updateItem(message);
        }
    }
}
