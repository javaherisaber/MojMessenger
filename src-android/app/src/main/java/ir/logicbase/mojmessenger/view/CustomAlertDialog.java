package ir.logicbase.mojmessenger.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * Created by Mahdi on 7/28/2017.
 * make an alert dialog material looking
 */
public class CustomAlertDialog {

    private Context context;
    private Typeface typeface;


    public CustomAlertDialog(Context context) {
        this.context = context;
        typeface = TypefaceManager.get(context, context.getString(R.string.font_iran_sans));
    }

    /**
     * @param title will be assigned to TextView
     * @return customized TextView for using in alert dialog
     */
    public TextView getTitleText(String title) {
        TextView dialogTitle = new TextView(context);
        dialogTitle.setText(title);
        dialogTitle.setGravity(Gravity.CENTER);
        dialogTitle.setTextSize(25);
        dialogTitle.setPadding(0, 0, 25, 0);
        dialogTitle.setTypeface(typeface);
        dialogTitle.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        dialogTitle.setTextColor(Color.WHITE);
        return dialogTitle;
    }

    /**
     * @param alertDialog child views will be customized looking
     */
    public void setDialogStyle(AlertDialog alertDialog) {
        TextView message = (TextView) alertDialog.findViewById(android.R.id.message);
        if (message != null) {
            message.setTypeface(typeface);
            message.setTextSize(18);
        }

        Button btnPositive = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
        btnPositive.setTypeface(typeface);
        btnPositive.setTextSize(18);

        Button btnNegative = alertDialog.getButton(Dialog.BUTTON_NEGATIVE);
        btnNegative.setTypeface(typeface);
        btnNegative.setTextSize(18);

        Button btnNeutral = alertDialog.getButton(Dialog.BUTTON_NEUTRAL);
        btnNeutral.setTypeface(typeface);
        btnNeutral.setTextSize(18);

    }
}
