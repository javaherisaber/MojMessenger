package ir.logicbase.mojmessenger.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Warning, this gives a false sense of security.  If an attacker has enough access to
 * acquire your password store, then he almost certainly has enough access to acquire your
 * source binary and figure out your encryption key.  However, it will prevent casual
 * investigators from acquiring passwords, and thereby may prevent undesired negative
 * publicity.
 * Don't use anything you wouldn't want to
 * get out there if someone decompiled
 * your app.
 */
abstract class SecureSharedPreferences implements SharedPreferences {

    private static final String UTF8 = "utf-8";
    private static final char[] SECRET_PASS_CODE = {'#', '%', 'S', '@', '6', 'D', '^'}; // INSERT A RANDOM PASSWORD HERE.
    private static final String SECRET_KEY_FACTORY = "PBEWithMD5AndDES";

    private SharedPreferences delegate;
    private Context context;

    SecureSharedPreferences(Context context, SharedPreferences delegate) {
        this.delegate = delegate;
        this.context = context;
    }

    private String encrypt(String value) {

        try {
            final byte[] bytes = value != null ? value.getBytes(UTF8) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY);
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SECRET_PASS_CODE));
            Cipher pbeCipher = Cipher.getInstance(SECRET_KEY_FACTORY);
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(
                    Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes(UTF8), 20));
            // ToDo : use better AlgorithmParameterSpec to initialize this pbeCipher variable see this for help
            // https://developer.android.com/training/articles/user-data-ids.html
            return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP), UTF8);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private String decrypt(String value) {
        try {
            final byte[] bytes = value != null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY);
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SECRET_PASS_CODE));
            Cipher pbeCipher = Cipher.getInstance(SECRET_KEY_FACTORY);
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(
                    Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes(UTF8), 20));
            return new String(pbeCipher.doFinal(bytes), UTF8);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected class Editor implements SharedPreferences.Editor {
        private SharedPreferences.Editor delegate;

        @SuppressLint("CommitPrefEdits")
        Editor() {
            this.delegate = SecureSharedPreferences.this.delegate.edit();
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            delegate.putString(key, encrypt(Boolean.toString(value)));
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            delegate.putString(key, encrypt(Float.toString(value)));
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            delegate.putString(key, encrypt(Integer.toString(value)));
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            delegate.putString(key, encrypt(Long.toString(value)));
            return this;
        }

        @Override
        public Editor putString(String key, String value) {
            delegate.putString(key, encrypt(value));
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String s, Set<String> set) {
            return null;
        }

        @Override
        public void apply() {
            delegate.apply();
        }

        @Override
        public Editor clear() {
            delegate.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return delegate.commit();
        }

        @Override
        public Editor remove(String s) {
            delegate.remove(s);
            return this;
        }
    }

    public Editor edit() {
        return new Editor();
    }


    @Override
    public Map<String, ?> getAll() {
        throw new UnsupportedOperationException(); // left as an exercise to the reader
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? Boolean.parseBoolean(decrypt(v)) : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? Float.parseFloat(decrypt(v)) : defValue;
    }

    @Override
    public int getInt(String key, int defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? Integer.parseInt(decrypt(v)) : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? Long.parseLong(decrypt(v)) : defValue;
    }

    @Override
    public String getString(String key, String defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? decrypt(v) : defValue;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String s, Set<String> set) {
        return null;
    }

    @Override
    public boolean contains(String s) {
        return delegate.contains(s);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        delegate.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        delegate.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

}