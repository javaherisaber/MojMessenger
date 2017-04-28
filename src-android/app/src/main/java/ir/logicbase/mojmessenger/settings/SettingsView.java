package ir.logicbase.mojmessenger.settings;

/**
 * Created by Mahdi on 12/10/2017.
 * Specify the contract between the view and the presenter
 */

interface SettingsView {

    void showUploadFailedMessage();

    void showInternetNotAvailableMessage();

    void updatePhoto(String url);

    void showFileNotFound();
}
