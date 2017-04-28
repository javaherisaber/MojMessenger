package ir.logicbase.mojmessenger.settings.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.NumberPicker;
import android.widget.TextView;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.util.TypefaceManager;
import ir.logicbase.mojmessenger.view.CustomAlertDialog;

public class CustomNumberPickerPreference extends Preference {

    private TextView summary, title;
    private static final int DEFAULT_VALUE = 1;  // trivial to satisfy methods (defaultValue is defined in xml)
    private int minValue;
    private int maxValue;
    private int currentValue;

    public CustomNumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.item_preference_number_picker);

        // retrieve custom attributes
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);
        minValue = Integer.valueOf(a.getString(R.styleable.NumberPickerPreference_minValue));
        maxValue = Integer.valueOf(a.getString(R.styleable.NumberPickerPreference_maxValue));
        a.recycle();
    }

    public CustomNumberPickerPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.item_preference_number_picker);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            // Restore existing state
            currentValue = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            currentValue = (Integer) defaultValue;
            persistInt(currentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_VALUE);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        title = (TextView) holder.findViewById(android.R.id.title);
        summary = (TextView) holder.findViewById(android.R.id.summary);
        summary.setText(String.valueOf(currentValue));
        Typeface typeface = TypefaceManager.get(getContext(), getContext().getString(R.string.font_iran_sans));
        title.setTypeface(typeface);
        summary.setTypeface(typeface);
    }

    @Override
    protected void onClick() {
        final NumberPicker picker = new NumberPicker(getContext());
        picker.setMaxValue(maxValue);
        picker.setMinValue(minValue);
        picker.setWrapSelectorWheel(false);
        picker.setValue(currentValue);

        final CustomAlertDialog customAlertDialog = new CustomAlertDialog(getContext());
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setCustomTitle(customAlertDialog.getTitleText(title.getText().toString()))
                .setView(picker)
                .setCancelable(true)
                .setPositiveButton(getContext().getString(R.string.done), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int selectedValue = picker.getValue();
                        persistInt(selectedValue);
                        currentValue = selectedValue;
                        summary.setText(String.valueOf(selectedValue));
                    }
                })
                .show();
        customAlertDialog.setDialogStyle(dialog);
    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readInt();  // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeInt(value);  // Change this to write the appropriate data type
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent,
            // use superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current
        // setting value
        myState.value = currentValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        currentValue = myState.value;
    }
}