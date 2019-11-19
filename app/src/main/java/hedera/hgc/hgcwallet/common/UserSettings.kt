/*
 *
 *  Copyright 2019 Hedera Hashgraph LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package hedera.hgc.hgcwallet.common

import android.content.SharedPreferences
import android.preference.PreferenceManager

import org.json.JSONException
import org.json.JSONObject

import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.modals.Exchange

object UserSettings {

    const val KEY_AUTH_TYPE = "key_auth_type"
    const val KEY_USER_NAME = "key_user_name"
    const val KEY_INTENT_URL = "key_intent_url"
    const val KEY_BRANCH_PARAMS = "key_barch_params"
    const val KEY_ASKED_FOR_QUERY_COST_WARNING = "key_asked_for_query_cost_warning"
    const val KEY_HAS_SHOWN_BIP39_MNEMONIC = "key_has_shown_bip39_mnemonic_v2"
    const val KEY_NEEDS_TO_SHOW_BIP39_MNEMONIC = "key_needs_to_show_bip39_mnemonic"
    const val KEY_LAST_SYNC_NODES_AT = "key_last_sync_nodes_at"
    const val KEY_EXCHANGE_RATE_CENT_EQUIV_CURRENT = "exchange_rate_cent_equiv_current"
    const val KEY_EXCHANGE_RATE_CENT_EQUIV_NEXT = "exchange_rate_cent_equiv_next"
    const val KEY_EXCHANGE_RATE_HBAR_EQUIV_CURRENT = "exchange_rate_hbar_equiv_current"
    const val KEY_EXCHANGE_RATE_HBAR_EQUIV_NEXT = "exchange_rate_hbar_equiv_next"
    const val KEY_EXCHANGE_RATE_EXP_TIME_SECONDS_CURRENT = "exchange_rate_exp_time_seconds_current"
    const val KEY_EXCHANGE_RATE_EXP_TIME_SECONDS_NEXT = "exchange_rate_exp_time_seconds_next"


    const val KEY_BITREX_EXCHANGE_RATE_DATA = "bitrex_exchange_rate_data"
    const val KEY_LIQUID_EXCHANGE_RATE_DATA = "liquid_exchange_rate_data"
    const val KEY_OKCOIN_EXCHANGE_RATE_DATA = "okcoin_exchange_rate_data"


    const val KEY_BITREX_EXCHANGE_RATE_DATE = "bitrex_exchange_rate_date"
    const val KEY_LIQUID_EXCHANGE_RATE_DATE = "liquid_exchange_rate_date"
    const val KEY_OKCOIN_EXCHANGE_RATE_DATE = "okcoin_exchange_rate_date"


    const val KEY_PIN_LENGTH = "pin_length"

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

    fun getLongValue(key: String, default: Long = -1L): Long {
        return sharedPref.getLong(key, default)
    }

    fun getDoubleValue(key: String, default: Double = -1.0): Double {
        return sharedPref.getDouble(key, default)
    }

    fun getIntValue(key: String, default: Int = -1): Int {
        return sharedPref.getInt(key, default)
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

    fun setValue(key: String, value: Int) {
        val editor = sharedPref.edit()
        editor.putInt(key, value)
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

    fun setValue(key: String, value: Double) {
        val editor = sharedPref.edit()
        editor.putDouble(key, value)
        editor.commit()
    }

    fun clear() {
        sharedPref.edit().apply {
            clear()
            commit()
        }
    }

    fun setExchangeRateData(exchange: Exchange, data: String) {
        val (dataKey, dateKey) = when (exchange) {
            Exchange.Bitrex -> KEY_BITREX_EXCHANGE_RATE_DATA to KEY_BITREX_EXCHANGE_RATE_DATE

            Exchange.Okcoin -> KEY_OKCOIN_EXCHANGE_RATE_DATA to KEY_OKCOIN_EXCHANGE_RATE_DATE

            Exchange.Liquid -> KEY_LIQUID_EXCHANGE_RATE_DATA to KEY_LIQUID_EXCHANGE_RATE_DATE

        }

        setValue(dataKey, data)
        setValue(dateKey, Singleton.getCurrentUTCSeconds())

    }

    fun getExchangeRateData(exchange: Exchange): Pair<String, Long>? {

        val (dataKey, dateKey) = when (exchange) {
            Exchange.Bitrex -> KEY_BITREX_EXCHANGE_RATE_DATA to KEY_BITREX_EXCHANGE_RATE_DATE

            Exchange.Okcoin -> KEY_OKCOIN_EXCHANGE_RATE_DATA to KEY_OKCOIN_EXCHANGE_RATE_DATE

            Exchange.Liquid -> KEY_LIQUID_EXCHANGE_RATE_DATA to KEY_LIQUID_EXCHANGE_RATE_DATE

        }

        return (getValue(dataKey) to getLongValue(dateKey)).let { if (it.first != null && it.second > 0) it.first!! to it.second else null }
    }
}

fun SharedPreferences.Editor.putDouble(key: String, double: Double) =
        putLong(key, java.lang.Double.doubleToRawLongBits(double))

fun SharedPreferences.getDouble(key: String, default: Double) =
        java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))
