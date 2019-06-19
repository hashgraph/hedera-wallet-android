package opencrowd.hgc.hgcwallet.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import opencrowd.hgc.hgcwallet.App;

public class UserSettings {
    public static final @NonNull String KEY_AUTH_TYPE = "key_auth_type";
    public static final @NonNull String KEY_USER_NAME = "key_user_name";
    public static final @NonNull String KEY_INTENT_URL = "key_intent_url";
    public static final @NonNull String KEY_BRANCH_PARAMS = "key_barch_params";
    public static final @NonNull String KEY_ASKED_FOR_QUERY_COST_WARNING = "key_asked_for_query_cost_warning";
    public static final @NonNull String KEY_HAS_SHOWN_BIP39_MNEMONIC = "key_has_shown_bip39_mnemonic";

    public static @NonNull UserSettings instance = new UserSettings();

    private @NonNull SharedPreferences sharedPref;

    public  UserSettings() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(App.instance);;
    }

    @Nullable
    public String getValue(@NonNull String key) {
        String value = sharedPref.getString(key,null);
        return value;
    }

    public boolean getBoolValue(@NonNull String key) {
        return sharedPref.getBoolean(key,false);
    }

    @Nullable
    public void resetValue(@NonNull String key) {
        setValue(key,"");
    }

    public void setValue(@NonNull String key, @NonNull String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void setValue(@NonNull String key, @NonNull boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    @Nullable
    public JSONObject getJSONValue(@NonNull String key) {
        String value = getValue(key);
        if (value == null || value.length() == 0) return null;
        try {
            return new JSONObject(value);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setValue(@NonNull String key, @NonNull JSONObject value) {
        if (value != null)
            setValue(key,value.toString());
        else
            setValue(key,"");
    }
}
