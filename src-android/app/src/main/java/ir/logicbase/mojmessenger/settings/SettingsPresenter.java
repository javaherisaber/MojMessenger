package ir.logicbase.mojmessenger.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.InputStream;

import ir.logicbase.mojmessenger.socket.PhotoUploader;
import ir.logicbase.mojmessenger.socket.UploadPhotoCallback;
import ir.logicbase.mojmessenger.util.AppExecutors;

/**
 * Created by Mahdi on 12/10/2017.
 * Listens for users's actions from the UI {@link ActivitySettings}, retrieves the data and updates
 * the UI as required.
 */

class SettingsPresenter {

    @Nullable
    private SettingsView view;

    SettingsPresenter(@Nullable SettingsView view) {
        this.view = view;
    }

    void startListening(SettingsView view) {
        this.view = view;
    }

    void stopListening() {
        view = null;
    }

    void uploadPhoto(Context context, String phone, Uri photoUri) {
        try {
            InputStream stream = context.getContentResolver().openInputStream(photoUri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            if (bitmap.getWidth() > 512 && bitmap.getHeight() > 512) {
                bitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true);
            }
            PhotoUploader.uploadPhoto(context, phone, new AppExecutors(), bitmap, uploadPhotoCallback);
        } catch (FileNotFoundException e) {
            if (view != null) {
                view.showFileNotFound();
            }
        }
    }


    private UploadPhotoCallback uploadPhotoCallback = new UploadPhotoCallback() {
        @Override
        public void internetNotConnected() {
            if (view != null) {
                view.showInternetNotAvailableMessage();
            }
        }

        @Override
        public void uploadSuccessful(String photoUrl) {
            if (view != null) {
                view.updatePhoto(photoUrl);
            }
        }

        @Override
        public void uploadFailed() {
            if (view != null) {
                view.showUploadFailedMessage();
            }
        }
    };
}
