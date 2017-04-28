package ir.logicbase.mojmessenger.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.view.CustomAlertDialog;

/**
 * Created by Mahdi on 7/28/2017.
 * all permission things goes here
 */

public class PermissionsRequest {

    private static final int READ_CONTACTS_KEY = 1;
    private static final int WRITE_EXTERNAL_STORAGE_KEY = 2;

    private static final String PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    /**
     * @return True if permission granted , False if not and tries to get permission
     */
    public static boolean checkReadContactsPermission(Context context, final Activity activity) {
        return checkPermission(context, activity, PERMISSION_READ_CONTACTS
                , context.getString(R.string.request_read_contacts_guide), READ_CONTACTS_KEY);
    }

    /**
     * @return True if permission granted , False if not and tries to get permission
     */
    public static boolean checkWriteExternalStoragePermission(Context context, final Activity activity) {
        return checkPermission(context, activity, PERMISSION_WRITE_EXTERNAL_STORAGE,
                context.getString(R.string.request_write_external_storage_guide), WRITE_EXTERNAL_STORAGE_KEY);
    }

    /**
     * @param permission used to grant from permission manager
     * @param explanation tell user why you need this privilege
     * @param key permission developer defined key in order to use at callback method
     * @return True if permission granted , False if not and tries to get permission
     */
    private static boolean checkPermission(Context context, Activity activity, String permission, String explanation, int key) {
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showAlertDialog(context, activity, explanation);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},
                        key);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * this dialog guide user to app settings in order to grant permission manually
     * @param explanation tell user why you need this privilege
     */
    private static void showAlertDialog(Context context, final Activity activity, String explanation) {
        CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCustomTitle(customAlertDialog.getTitleText(context.getString(R.string.guide)))
                .setMessage(explanation)
                .setCancelable(true)
                .setPositiveButton(context.getString(R.string.settings), (dialog1, id) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                })
                .show();
        customAlertDialog.setDialogStyle(dialog);
    }
}
