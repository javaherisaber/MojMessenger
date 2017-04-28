package ir.logicbase.mojmessenger.settings;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.contact.ImageActivity;
import ir.logicbase.mojmessenger.util.PermissionsRequest;
import ir.logicbase.mojmessenger.util.PrefManager;
import ir.logicbase.mojmessenger.util.TypefaceManager;

public class ActivitySettings extends AppCompatActivity implements SettingsView {

    private static final int RESULT_LOAD_IMG = 1;
    private ImageView profilePic;
    private SettingsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // init views
        Toolbar toolbar = findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        TextView title = findViewById(R.id.txtView_settings_title);
        profilePic = findViewById(R.id.imView_settings_profile);
        FloatingActionButton uploadPhoto = findViewById(R.id.fab_settings);

        // init toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitleEnabled(false);
        PrefManager pref = new PrefManager(getApplicationContext());
        Typeface typeface = TypefaceManager.get(this, getString(R.string.font_iran_sans));
        title.setTypeface(typeface);
        title.setText(pref.getPhoneNumber());

        String profilePicUrl = new PrefManager(this).getProfilePic();
        if (!profilePicUrl.equals("")) {
            Picasso.with(this)
                    .load(profilePicUrl)
                    .placeholder(R.drawable.default_person_pic)
                    .error(R.drawable.default_person_pic)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(profilePic);
        }

        uploadPhoto.setOnClickListener(v -> {
            if (PermissionsRequest.checkWriteExternalStoragePermission(this, this)) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        findViewById(R.id.imView_settings_profile).setOnClickListener(v -> {
            if (!profilePicUrl.equals("")) {
                Intent intent = new Intent(ActivitySettings.this, ImageActivity.class);
                intent.putExtra(ImageActivity.INTENT_KEY_IMAGE_URL, profilePicUrl);
                startActivity(intent);
            }
        });

        presenter = new SettingsPresenter(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.startListening(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            presenter.uploadPhoto(this, new PrefManager(this).getPhoneNumber(), imageUri);
        } else if (requestCode == RESULT_LOAD_IMG){
            Toast.makeText(this, "تصویری انتخاب نشد", Toast.LENGTH_LONG).show();
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
    public void showUploadFailedMessage() {
        Toast.makeText(this, "خطا در آپلود تصویر ، دوباره امتحان کنید !", Toast.LENGTH_LONG).show();
    }

    @Override
    public void showInternetNotAvailableMessage() {
        Toast.makeText(this, "اینترنت در دسترس نیست !", Toast.LENGTH_LONG).show();
    }

    @Override
    public void updatePhoto(String url) {
        Picasso.with(this)
                .load(url)
                .placeholder(R.drawable.default_person_pic)
                .error(R.drawable.default_person_pic)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(profilePic);
    }

    @Override
    public void showFileNotFound() {
        Toast.makeText(this, "تصویری یافت نشد !", Toast.LENGTH_LONG).show();
    }
}
