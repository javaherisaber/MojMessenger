package ir.logicbase.mojmessenger.profile;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.contact.ImageActivity;
import ir.logicbase.mojmessenger.conversation.ActivityConversation;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.util.Time;
import ir.logicbase.mojmessenger.util.TypefaceManager;

public class ActivityProfile extends AppCompatActivity implements ProfileView{

    public static final String INTENT_KEY_CONTACT_ID = "ContactId";
    public static final String INTENT_KEY_NAME = "Name";
    public static final String INTENT_KEY_LAST_SEEN = "lastSeen";
    public static final String INTENT_KEY_PIC = "Pic";
    public static final String INTENT_KEY_PHONE = "Phone";
    public static final String INTENT_KEY_IS_ONLINE = "isOnline";

    private ProfilePresenter presenter;

    private ContactEntity contact;
    private TextView txtLastSeen;
    private ImageView imgProfilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final int contactId = getIntent().getIntExtra(INTENT_KEY_CONTACT_ID, 0);
        final String name = getIntent().getStringExtra(INTENT_KEY_NAME);
        final String lastSeen = getIntent().getStringExtra(INTENT_KEY_LAST_SEEN);
        final String pic = getIntent().getStringExtra(INTENT_KEY_PIC);
        final String phone = getIntent().getStringExtra(INTENT_KEY_PHONE);
        boolean isOnline = getIntent().getBooleanExtra(INTENT_KEY_IS_ONLINE, false);
        contact = new ContactEntity(contactId, name, phone, pic, lastSeen, isOnline);

        FloatingActionButton fab = findViewById(R.id.fab_profile);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ActivityProfile.this, ActivityConversation.class);
            intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_ID, contact.getId());
            intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_NAME, contact.getName());
            intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_LAST_SEEN, contact.getLastSeen());
            intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_PIC, contact.getPic());
            intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_PHONE, contact.getPhone());
            intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_IS_ONLINE, contact.isOnline());
            startActivity(intent);
            finish();
        });

        findViewById(R.id.imView_profile_pic).setOnClickListener(v -> {
            Intent intent = new Intent(ActivityProfile.this, ImageActivity.class);
            intent.putExtra(ImageActivity.INTENT_KEY_IMAGE_URL, contact.getPic());
            startActivity(intent);
        });

        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        TextView txtName = findViewById(R.id.title_profile);
        txtName.setText(name);
        TextView txtViewPhone = findViewById(R.id.txtView_profile_phone);
        txtViewPhone.setText(phone);
        txtLastSeen = findViewById(R.id.subTitle_profile);
        if (contact.isOnline()) {
            txtLastSeen.setText("آنلاین");
        } else {
            String lastSeenLabel = "آخرین بازدید : " + Time.getLastSeenLabel(lastSeen);
            txtLastSeen.setText(lastSeenLabel);
        }
        imgProfilePic = findViewById(R.id.imView_profile_pic);
        Picasso.with(this)
                .load(pic)
                .placeholder(R.drawable.default_person_pic)
                .error(R.drawable.default_person_pic)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(imgProfilePic);



        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitleEnabled(false);

        Typeface typeface = TypefaceManager.get(this, getString(R.string.font_iran_sans));
        txtName.setTypeface(typeface);
        txtLastSeen.setTypeface(typeface);

        presenter = new ProfilePresenter(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.startListening(this);
        presenter.syncProfile(contact.getPhone());
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
        }
        return false;
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
}
