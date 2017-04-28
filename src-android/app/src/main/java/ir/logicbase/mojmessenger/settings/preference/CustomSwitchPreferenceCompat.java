package ir.logicbase.mojmessenger.settings.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.util.TypefaceManager;

public class CustomSwitchPreferenceCompat extends SwitchPreferenceCompat {

    private SwitchCompat switchCompat;
    private static final boolean DEFAULT_VALUE = true;  // trivial to satisfy methods (defaultValue is defined in xml)
    private boolean currentValue;

    public CustomSwitchPreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.item_preference_switch_compat);
    }

    public CustomSwitchPreferenceCompat(Context context) {
        super(context);
        setLayoutResource(R.layout.item_preference_switch_compat);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            // Restore existing state
            currentValue = this.getPersistedBoolean(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            currentValue = (Boolean) defaultValue;
            persistBoolean(currentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getBoolean(index, DEFAULT_VALUE);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        switchCompat = (SwitchCompat) holder.findViewById(R.id.switchPreference);
        Typeface typeface = TypefaceManager.get(getContext(), getContext().getString(R.string.font_iran_sans));
        titleView.setTypeface(typeface);
        summaryView.setTypeface(typeface);
        switchCompat.setChecked(currentValue);
    }

    @Override
    protected void onClick() {
        if(currentValue){
            persistBoolean(false);
            currentValue = false;
            switchCompat.setChecked(false);
        }else {
            persistBoolean(true);
            currentValue = true;
            switchCompat.setChecked(true);
        }
    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        boolean value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeByte((byte) (value ? 1 : 0));   // Change this to write the appropriate data type
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
