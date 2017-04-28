package ir.logicbase.mojmessenger.socket;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;

import ir.logicbase.mojmessenger.util.AppExecutors;
import ir.logicbase.mojmessenger.util.ApplicationLoader;
import ir.logicbase.mojmessenger.util.Connectivity;
import ir.logicbase.mojmessenger.util.PrefManager;

/**
 * Created by Mahdi on 12/10/2017.
 * upload any attachment to server
 */

public class PhotoUploader {

    private static final String SERVER_ADDRESS = "www.logicbase.ir";
    private static final int SERVER_PORT = 6701;

    public static void uploadPhoto(Context context, String phone, AppExecutors appExecutors, Bitmap bitmap,
                                   final UploadPhotoCallback callback) {
        final WeakReference<UploadPhotoCallback> uploadPhotoCallback = new WeakReference<>(callback);

        // upload file to server on Network thread
        if (Connectivity.isConnected(context)) {
            appExecutors.networkIO().execute(() -> {

                // convert bitmap to byteArray
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                String encodedString = Base64.encodeToString(byte_arr, 0);
                byte[] imageBytes = encodedString.getBytes(Charset.forName("UTF-8"));

                try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)){
                    HashMap<String, String> header = MessageHelper.getPacketHeader(MessageHelper.HEADER_VALUE_METHOD_UPLOAD_PHOTO,
                            imageBytes.length, phone);
                    byte[] packet = new PacketEntity(header, imageBytes).createPacket();
                    OutputStream out = socket.getOutputStream();
                    out.write(packet);
                    out.flush();
                    IncomingGateway.getInstance().setUploadPhotoListener( (photoUrl)-> {
                        // notify on the main thread
                        appExecutors.mainThread().execute(() -> {
                            final UploadPhotoCallback uploadCallback = uploadPhotoCallback.get();
                            if (uploadCallback != null) {
                                new PrefManager(ApplicationLoader.getInstance()).putProfilePic(photoUrl);
                                uploadCallback.uploadSuccessful(photoUrl);
                            }
                        });
                    });
                } catch (IOException e) {
                    // notify on the main thread
                    appExecutors.mainThread().execute(() -> {
                        final UploadPhotoCallback uploadCallback = uploadPhotoCallback.get();
                        if (uploadCallback != null) {
                            uploadCallback.uploadFailed();
                        }
                    });
                }
            });
        } else {
            // notify on the main thread
            appExecutors.mainThread().execute(() -> {
                final UploadPhotoCallback uploadCallback = uploadPhotoCallback.get();
                if (uploadCallback != null) {
                    uploadCallback.internetNotConnected();
                }
            });
        }
    }
}
