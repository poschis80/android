package xpshome.net.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Created by Christian Poschinger on 09.09.2015.
 */
public class SettingsManager {

    // TODO : add keystore feature to store cipher keys for encrypted content
    // TODO : add sqlite db as optional storage for settings/preferences  - check https://realm.io/
    // TODO : think about synchronize read and write access of settings  (shared preferences are by default thread-safe)

    private static final String TAG = "SettingsManager";
    private static final String PREF_FOLDER = "/shared_prefs/";
    public static final String MASTER_PREFERENCES = "master_shared_preferences";
    private static final String SettingsCryptoAlias = "XPSHOME_SCA";
    private SharedPreferences sharedPref;
    private Context context;

    private HashMap<String, SharedPreferences> customPreferences;
    private List<SharedPreferences.OnSharedPreferenceChangeListener> onChangeListeners;


    public SettingsManager(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences(
                MASTER_PREFERENCES, Context.MODE_PRIVATE);
        customPreferences = new HashMap<>();
        onChangeListeners = new ArrayList<>();
        initializeCustomPrefFiles();
    }

    private boolean initializeCustomPrefFiles() {
        File f = new File(context.getApplicationInfo().dataDir + PREF_FOLDER);
        if (f.isDirectory()) {
            File[] pf = f.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith(".xml") && !pathname.getName().contains(MASTER_PREFERENCES)) {
                        return true;
                    }
                    return false;
                }
            });

            for (File x : pf) {
                createNewSharedPreferences(x.getAbsoluteFile().getName().replace(".xml", ""));
            }

            return true;
        }

        return false;
    }

    public static final int RESULT_OK = 0;
    public static final int RESULT_EXISTING = 1;
    public static final int RESULT_FAILED = 2;

    private SharedPreferences getPreferencesFor(final String name) {
        if (name == null || name.compareTo(MASTER_PREFERENCES) == 0) {
            return sharedPref;
        }

        if (customPreferences.containsKey(name)) {
            return customPreferences.get(name);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public boolean preferenceGroupExists(String group) {
        return customPreferences.containsKey(group);
    }

    private int createNewSharedPreferences(final String name) {
        if (customPreferences.containsKey(name)) { return RESULT_EXISTING; }

        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        if (sp != null) {
            customPreferences.put(name, sp);
            for (SharedPreferences.OnSharedPreferenceChangeListener l : onChangeListeners) {
                registerChangeListener(sp, l);
            }
            return RESULT_OK;
        }
        return RESULT_FAILED;
    }

    @SuppressWarnings("unused")
    public boolean addNewPreferencesGroup(final String name) {
        return !customPreferences.containsKey(name) && createNewSharedPreferences(name) == RESULT_OK;
    }


    @SuppressWarnings("unused")
    public <T> T getValueFromPreferences(final String key, T defaultValue, boolean decrypt, @Nullable String cryptoAlias) {
        if (sharedPref.contains(key)) {
            return getValueFromPreferences(key, defaultValue, sharedPref, decrypt, cryptoAlias);
        } else {
            for (SharedPreferences p : customPreferences.values()) {
                if (p.contains(key)) {
                    return getValueFromPreferences(key, defaultValue, p, decrypt, cryptoAlias);
                }
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unused")
    public <T> T getValueFromPreferences(final String key, T defaultValue) {
        if (sharedPref.contains(key)) {
            return getValueFromPreferences(key, defaultValue, sharedPref, false, null);
        } else {
            for (SharedPreferences p : customPreferences.values()) {
                if (p.contains(key)) {
                    return getValueFromPreferences(key, defaultValue, p, false, null);
                }
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unused")
    public <T> T getValueFromPreferences(final String key, T defaultValue, final String preferenceGroup, boolean decrypt, @Nullable String cryptoAlias) {
        if (preferenceGroup == null || preferenceGroup.isEmpty()) {
            return getValueFromPreferences(key, defaultValue, sharedPref, decrypt, cryptoAlias);
        } else if (customPreferences.containsKey(preferenceGroup)) {
            return getValueFromPreferences(key, defaultValue, customPreferences.get(preferenceGroup), decrypt, cryptoAlias);
        }
        return defaultValue;
    }

    @SuppressWarnings("unused")
    public <T> T getValueFromPreferences(final String key, T defaultValue, final String preferenceGroup) {
        if (preferenceGroup == null || preferenceGroup.isEmpty()) {
            return getValueFromPreferences(key, defaultValue, sharedPref, false, null);
        } else if (customPreferences.containsKey(preferenceGroup)) {
            return getValueFromPreferences(key, defaultValue, customPreferences.get(preferenceGroup), false, null);
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private <T> T getValueFromPreferences(final String key, T defaultValue, SharedPreferences preferences, boolean decrypt, String cryptoAlias) {
        Map<String, ?> prefs = preferences.getAll();
        if (prefs.containsKey(key)) {
            if (decrypt) {
                byte[] enc = Base64.decode((String)prefs.get(key), Base64.DEFAULT);
                byte[] raw = Security.NoUserInteraction.decrypt(cryptoAlias != null ? cryptoAlias : SettingsCryptoAlias, enc);
                return (T) Security.Util.fromByteArray(raw, defaultValue);
            } else {
                if (defaultValue instanceof byte[]) {
                    return (T) Base64.decode((String)prefs.get(key), Base64.DEFAULT);
                }
                return (T) prefs.get(key);
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unused")
    public <T> boolean storeValueInSharedPreferences(final String key, T value, boolean encrypt, @Nullable String cryptoAlias) {
        return storeValueInSharedPreferences(key, value, null, encrypt, cryptoAlias);
    }

    @SuppressWarnings("unused")
    public <T> boolean storeValueInSharedPreferences(final String key, T value) {
        return storeValueInSharedPreferences(key, value, null, false, null);
    }

    @SuppressWarnings("unused")
    public <T> boolean storeValueInSharedPreferences(final String key, T value, final String preferencesGroup) {
        return storeValueInSharedPreferences(key, value, preferencesGroup, false, null);
    }

    @SuppressWarnings("unused unchecked")
    public <T> boolean storeValueInSharedPreferences(final String key, T value, final String preferencesGroup, boolean encrypt, @Nullable String cryptoAlias) {
        SharedPreferences p = getPreferencesFor(preferencesGroup);
        if (p != null) {
            SharedPreferences.Editor editor = p.edit();

            if (encrypt) {
                byte[] raw = Security.Util.toByteArray(value);
                editor.putString(key, Base64.encodeToString(Security.NoUserInteraction.encrypt(cryptoAlias != null ? cryptoAlias : SettingsCryptoAlias, raw), Base64.DEFAULT));
            } else {
                if (value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (value instanceof Long) {
                    editor.putLong(key, (Long) value);
                } else if (value instanceof Integer) {
                    editor.putInt(key, (Integer) value);
                } else if (value instanceof Float) {
                    editor.putFloat(key, (Float) value);
                } else if (value instanceof String) {
                    editor.putString(key, (String) value);
                } else if (value instanceof Set<?>) {
                    editor.putStringSet(key, (Set<String>) value);
                } else if (value instanceof byte[]) {
                    editor.putString(key, Base64.encodeToString((byte[])value, Base64.DEFAULT));
                } else {
                    return false;
                }
            }
            editor.apply();
            return true;
        }

        return false;
    }

    @SuppressWarnings("unused")
    public boolean hasSetting(final String key) {
        if (sharedPref.contains(key)) {
            return true;
        }
        for (SharedPreferences p : customPreferences.values()) {
            if (p.contains(key)) {
                return true;
            }
        }
        return false;
    }
    @SuppressWarnings("unused")
    public boolean hasSetting(final String key, final String preferencesGroup) {
        SharedPreferences p = getPreferencesFor(preferencesGroup);
        return p != null && p.contains(key);
    }


    @SuppressWarnings("unused")
    public boolean removeSetting(String key) {
        if (sharedPref.contains(key)) {
            return removeSettings(key, sharedPref);
        }

        for (SharedPreferences p : customPreferences.values()) {
            if (p.contains(key)) {
                return removeSettings(key, p);
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public boolean removeSettings(final String key, final String preferencesGroup) {
        return removeSettings(key, getPreferencesFor(preferencesGroup));
    }

    private boolean removeSettings(final String key, SharedPreferences preferences) {
        if (preferences != null && preferences.contains(key)) {
            preferences.edit().remove(key).apply();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    public void registerChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        onChangeListeners.add(listener);
        registerChangeListener(sharedPref, listener);
        for (SharedPreferences p : customPreferences.values()) {
            registerChangeListener(p, listener);
        }
    }

    private void registerChangeListener(SharedPreferences preferences, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @SuppressWarnings("unused")
    public void unregisterChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
        for (SharedPreferences p : customPreferences.values()) {
            p.unregisterOnSharedPreferenceChangeListener(listener);
        }
        onChangeListeners.remove(listener);
    }
}
