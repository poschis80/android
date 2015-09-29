package xpshome.net.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Christian Poschinger on 09.09.2015.
 */
public class SettingsManager {
    private static final String TAG = "SettingsManager";
    public static final String MASTER_PREFERENCES = "master_shared_preferences";
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
    public <T> T getValueFromPreferences(final String key, T defaultValue) {
        if (sharedPref.contains(key)) {
            return getValueFromPreferences(key, defaultValue, sharedPref);
        } else {
            for (SharedPreferences p : customPreferences.values()) {
                if (p.contains(key)) {
                    return getValueFromPreferences(key, defaultValue, p);
                }
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unused")
    public <T> T getValueFromPreferences(final String key, T defaultValue, final String preferenceGroup) {
        if (preferenceGroup == null || preferenceGroup.isEmpty()) {
            return getValueFromPreferences(key, defaultValue, sharedPref);
        } else if (customPreferences.containsKey(preferenceGroup)) {
            return getValueFromPreferences(key, defaultValue, customPreferences.get(preferenceGroup));
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private <T> T getValueFromPreferences(final String key, T defaultValue, SharedPreferences preferences) {
        Map<String, ?> prefs = preferences.getAll();
        if (prefs.containsKey(key)) {
            return (T) prefs.get(key);
        }
        return defaultValue;
    }

    @SuppressWarnings("unused")
    public <T> boolean storeValueInSharedPreferences(final String key, T value) {
        return storeValueInSharedPreferences(key, value, null);
    }

    @SuppressWarnings("unused unchecked")
    public <T> boolean storeValueInSharedPreferences(final String key, T value, final String preferencesGroup) {
        SharedPreferences p = getPreferencesFor(preferencesGroup);
        if (p != null) {
            SharedPreferences.Editor editor = p.edit();
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
            } else {
                return false;
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
