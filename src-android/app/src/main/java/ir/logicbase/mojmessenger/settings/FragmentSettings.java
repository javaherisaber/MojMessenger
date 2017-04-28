package ir.logicbase.mojmessenger.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.util.ApplicationLoader;
import ir.logicbase.mojmessenger.util.PermissionsRequest;
import ir.logicbase.mojmessenger.util.PrefManager;
import ir.logicbase.mojmessenger.view.CustomAlertDialog;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Mahdi on 6/16/2017.
 * display app settings
 */

public class FragmentSettings extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

    private static final int RESULT_LOAD_IMG = 2;
    private Preference pickPhoto, signOut;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);

        pickPhoto = findPreference(getString(R.string.pref_background_pic));
        signOut = findPreference(getString(R.string.pref_sign_out));

        pickPhoto.setOnPreferenceClickListener(this);
        signOut.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals(pickPhoto.getKey())) {
            if (PermissionsRequest.checkWriteExternalStoragePermission(getContext(), getActivity())) {
                onPickPhotoClick();
            }
            return true;
        } else if (key.equals(signOut.getKey())) {
            onSignOutClick();
            return true;
        }
        return false;
    }

    public void onSignOutClick() {
        final CustomAlertDialog customAlertDialog = new CustomAlertDialog(getContext());
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setCustomTitle(customAlertDialog.getTitleText("خروج از حساب"))
                .setMessage(R.string.explanation_sign_out)
                .setCancelable(true)
                .setPositiveButton(R.string.yes, (dialog12, id) -> {
                    // TODO: 8/15/2017 reset all your preferences related to login here
                    // redirect to sms verification activity
                    // FIXME: 12/9/2017 makeRestartActivityTask deprecated
//                    Intent intent = IntentCompat.makeRestartActivityTask(
//                            new ComponentName(getContext(), ActivitySmsVerification.class));
//                    getContext().startActivity(intent);
                })
                .setNegativeButton(R.string.no, (dialog1, which) -> dialog1.dismiss())
                .show();
        customAlertDialog.setDialogStyle(dialog);
    }

    public void onPickPhotoClick() {
        ImageView imViewPicPhoto = new ImageView(getContext());
        PrefManager pref = new PrefManager(getContext());
        String imagePath = pref.getSettingsPrefChatBackgroundPic();
        if (imagePath == null || imagePath.equals("null")) {
            imViewPicPhoto.setImageResource(R.drawable.conversation_bg);
        } else {
                Bitmap selectedImage = BitmapFactory.decodeFile(imagePath);
                imViewPicPhoto.setImageBitmap(selectedImage);
        }

        final CustomAlertDialog customAlertDialog = new CustomAlertDialog(getContext());
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setCustomTitle(customAlertDialog.getTitleText("تصویر پشت زمینه چت"))
                .setView(imViewPicPhoto)
                .setCancelable(true)
                .setPositiveButton(R.string.change, (dialog1, id) -> {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
                })
                .setNegativeButton(R.string.reset, (dialog2, id) -> {
                    imViewPicPhoto.setImageResource(R.drawable.conversation_bg);
                    pref.putSettingsPrefChatBackgroundPic(null);
                    Toast.makeText(getContext(), "تصویر پشت زمینه چت بازنشانی یافت", Toast.LENGTH_LONG).show();
                } )
                .show();
        customAlertDialog.setDialogStyle(dialog);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            new SaveImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageUri);
        } else if (requestCode == RESULT_LOAD_IMG){
            Toast.makeText(getActivity(), "تصویری انتخاب نشد", Toast.LENGTH_LONG).show();
        }
    }

    private class SaveImageTask extends AsyncTask<Uri, Void, String> {

        private FrameLayout progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = getActivity().findViewById(R.id.frameCircularProgress);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Uri... image) {
            try {
                InputStream stream = getContext().getContentResolver().openInputStream(image[0]);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                // FIXME: 12/4/2017 set correct width and height for different screen sizes
                if (bitmap.getWidth() > 768 && bitmap.getHeight() > 1280) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, 768, 1280, true);
                }
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/MojMessenger/background");
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                String fileName = "MojBackground.jpg";
                File file = new File(myDir, fileName);
                if (file.exists()) {
                    file.delete();
                }
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                return file.toString();
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String imagePath) {
            super.onPostExecute(imagePath);
            progress.setVisibility(View.GONE);
            Context context = ApplicationLoader.getInstance().getApplicationContext();
            if (imagePath != null) {
                PrefManager pref = new PrefManager(context);
                pref.putSettingsPrefChatBackgroundPic(imagePath);
                Toast.makeText(context, "تصویر پشت زمینه چت تغییر کرد", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "خطایی رخ داد", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
