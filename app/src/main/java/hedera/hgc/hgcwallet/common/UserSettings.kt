package hedera.hgc.hgcwallet.common

import android.content.SharedPreferences
import android.preference.PreferenceManager

import org.json.JSONException
import org.json.JSONObject

import hedera.hgc.hgcwallet.App

object UserSettings {

    const val KEY_AUTH_TYPE = "key_auth_type"
    const val KEY_USER_NAME = "key_user_name"
    const val KEY_INTENT_URL = "key_intent_url"
    const val KEY_BRANCH_PARAMS = "key_barch_params"
    const val KEY_ASKED_FOR_QUERY_COST_WARNING = "key_asked_for_query_cost_warning"
    const val KEY_HAS_SHOWN_BIP39_MNEMONIC = "key_has_shown_bip39_mnemonic_v2"
    const val KEY_NEEDS_TO_SHOW_BIP39_MNEMONIC = "key_needs_to_show_bip39_mnemonic"
    const val KEY_LAST_SYNC_NODES_AT = "key_last_sync_nodes_at"

    const val KEY_DEFAULT_FEE = "key_default_fee_v1"

    private val sharedPref: SharedPreferences

    init {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(App.instance)
    }

    fun getValue(key: String): String? {
        return sharedPref.getString(key, null)
    }

    fun getBoolValue(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }

    fun getLongValue(key: String): Long {
        return sharedPref.getLong(key, -1)
    }


    fun resetValue(key: String) {
        setValue(key, "")
    }

    fun setValue(key: String, value: String) {
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun setValue(key: String, value: Boolean) {
        val editor = sharedPref.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun setValue(key: String, value: Long) {
        val editor = sharedPref.edit()
        editor.putLong(key, value)
        editor.commit()
    }

    fun getJSONValue(key: String): JSONObject? {
        val value = getValue(key)
        if (value == null || value.isEmpty()) return null
        try {
            return JSONObject(value)
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }

    }

    fun setValue(key: String, value: JSONObject) {
        if (value != null)
            setValue(key, value.toString())
        else
            setValue(key, "")
    }
}
