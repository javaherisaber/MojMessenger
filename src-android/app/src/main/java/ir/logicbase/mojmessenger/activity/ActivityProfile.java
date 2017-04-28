package ir.logicbase.mojmessenger.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.util.Time;
import ir.logicbase.mojmessenger.util.TypefaceManager;

public class ActivityProfile extends AppCompatActivity {

    public static final String INTENT_KEY_CONTACT_ID = "ContactId";
    public static final String INTENT_KEY_NAME = "Name";
    public static final String INTENT_KEY_LAST_SEEN = "lastSeen";
    public static final String INTENT_KEY_PIC = "Pic";
    public static final String INTENT_KEY_PHONE = "Phone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final int contactId = getIntent().getIntExtra(INTENT_KEY_CONTACT_ID, -1);
        final String name = getIntent().getStringExtra(INTENT_KEY_NAME);
        final String lastSeen = getIntent().getStringExtra(INTENT_KEY_LAST_SEEN);
        final String pic = getIntent().getStringExtra(INTENT_KEY_PIC);
        final String phone = getIntent().getStringExtra(INTENT_KEY_PHONE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_profile);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ActivityProfile.this, ActivityConversation.class);
            intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_ID, contactId);
            intent.putExtra(ActivityConversation.INTENT_KEY_CONVERSATION_TITLE, name);
            intent.putExtra(ActivityConversation.INTENT_KEY_USER_LAST_SEEN, lastSeen);
            intent.putExtra(ActivityConversation.INTENT_KEY_CONVERSATION_PIC, pic);
            intent.putExtra(ActivityConversation.INTENT_KEY_USER_PHONE, phone);
            startActivity(intent);
            finish();
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        TextView title = (TextView) findViewById(R.id.title_profile);
        TextView subTitle = (TextView) findViewById(R.id.subTitle_profile);
        title.setText(name);
        String lastSeenLabel = "آخرین بازدید : " + Time.getLastSeenLabel(lastSeen);
        subTitle.setText(lastSeenLabel);
        TextView txtViewPhone = (TextView) findViewById(R.id.txtView_profile_phone);
        txtViewPhone.setText(phone);

        CircleImageView imViewProfilePic = (CircleImageView) findViewById(R.id.imView_profile_pic);
        Picasso.with(this)
                .load(pic)
                .placeholder(R.drawable.default_person_pic)
                .error(R.drawable.default_person_pic)
                .into(imViewProfilePic);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitleEnabled(false);

        Typeface typeface = TypefaceManager.get(this, getString(R.string.font_iran_sans));
        title.setTypeface(typeface);
        subTitle.setTypeface(typeface);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_edit: {
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                // TODO: 8/15/2017 implement this method
                return true;
            }
            case R.id.action_delete: {
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                // TODO: 8/15/2017 implement this method
                return true;
            }
            // case blocks for other MenuItems (if any)
        }
        return false;
    }
}
