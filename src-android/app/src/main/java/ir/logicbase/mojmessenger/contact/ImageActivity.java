package ir.logicbase.mojmessenger.contact;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import ir.logicbase.mojmessenger.R;

public class ImageActivity extends AppCompatActivity {

    public static final String INTENT_KEY_IMAGE_URL = "ImageUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        String pic = getIntent().getStringExtra(INTENT_KEY_IMAGE_URL);

        ImageView image = findViewById(R.id.imView_fullImage);
        Picasso.with(this)
                .load(pic)
                .placeholder(R.drawable.default_person_pic)
                .error(R.drawable.default_person_pic)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(image);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
}
