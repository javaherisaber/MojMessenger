package ir.logicbase.mojmessenger.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import ir.logicbase.mojmessenger.R;

/**
 * using android Shared Preferences to store information
 * in application SandBoxed and secured preferences
 * information such as settings, preferences,app behaviour
 * and public app instance specific ones goes inside this place
 */
public class PrefManager extends SecureSharedPreferences {

    private static final String PREF_ID = "pref";
    private static final String KEY_IS_REGISTERED = "isRegistered";
    private static final String KEY_PHONE_NUMBER = "phoneNumber";
    private static final String KEY_PROFILE_PIC = "profilePic";
    private Context context;

    public PrefManager(Context context){
        super(context, context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE));
        this.context = context;
    }

    public boolean getIsRegistered(){
        return getBoolean(KEY_IS_REGISTERED, false);
    }

    public void putIsRegistered(boolean isRegistered){
        edit().putBoolean(KEY_IS_REGISTERED, isRegistered).apply();
    }

    public String getProfilePic() {
        return getString(KEY_PROFILE_PIC, "");
    }

    public void putProfilePic(String value) {
        edit().putString(KEY_PROFILE_PIC, value).apply();
    }

    public String getPhoneNumber(){
        return getString(KEY_PHONE_NUMBER, null);
    }

    public void putPhoneNumber(String phone){
        edit().putString(KEY_PHONE_NUMBER, phone).apply();
    }

    public int getSettingsPrefConversationFontSize() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(context.getString(R.string.pref_conversation_font_size), 16);
    }

    public void putSettingsPrefConversationFontSize(int value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(context.getString(R.string.pref_conversation_font_size), value);
        editor.apply();
    }

    public String getSettingsPrefChatBackgroundPic() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(context.getString(R.string.pref_background_pic), null);
    }

    public void putSettingsPrefChatBackgroundPic(String value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(context.getString(R.string.pref_background_pic), value);
        editor.apply();
    }
}