package ir.logicbase.mojmessenger.socket;

/**
 * Created by Mahdi on 12/10/2017.
 * to inform uploader about the status
 */

public interface UploadPhotoCallback {

    void internetNotConnected();

    void uploadSuccessful(String photoUrl);

    void uploadFailed();
}
