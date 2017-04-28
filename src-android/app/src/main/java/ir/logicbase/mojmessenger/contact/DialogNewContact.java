package ir.logicbase.mojmessenger.contact;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.view.CustomAlertDialog;

/**
 * Created by Mahdi on 8/21/2017.
 * used in activity home and will be fired by fab
 */

public class DialogNewContact {

    public void showDialog(final Activity activity, final Context context) {
        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        View holder = inflater.inflate(R.layout.dialog_new_contact, null);
        final EditText firstName = holder.findViewById(R.id.edText_dialog_new_contact_firstName);
        final EditText lastName = holder.findViewById(R.id.edText_dialog_new_contact_lastName);
        final EditText phone = holder.findViewById(R.id.edText_dialog_new_contact_phone);

        CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCustomTitle(customAlertDialog.getTitleText(context.getString(R.string.dialog_new_contact_title)))
                .setView(holder)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.dialog_new_contact_create), (dialog1, id) -> {
                    if (isValidInput(firstName, phone)) {
                        String name = firstName.getText().toString() + " " + lastName.getText().toString();
                        boolean result = ContactHelper.insertNewContact(activity.getContentResolver(), name, phone.getText().toString());
                        if (result) {
                            Toast.makeText(context, context.getString(R.string.dialog_new_contact_successful)
                                    , Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "مخاطب موجود است", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.dialog_new_contact_check_inputs)
                                , Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(context.getString(R.string.abort), (dialog12, id) -> dialog12.dismiss()).show();
        customAlertDialog.setDialogStyle(dialog);
    }

    private boolean isValidInput(EditText firstName, EditText phone) {
        if (!firstName.getText().toString().isEmpty() && !phone.getText().toString().isEmpty()) {
            String phoneNumber = phone.getText().toString();
            return ContactHelper.isValidPhone(phoneNumber);
        } else {
            return false;
        }
    }
}
