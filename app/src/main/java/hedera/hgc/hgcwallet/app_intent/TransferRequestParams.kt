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

package hedera.hgc.hgcwallet.app_intent

import android.net.Uri

import org.json.JSONObject


import hedera.hgc.hgcwallet.modals.HGCAccountID
import javax.annotation.meta.When

class TransferRequestParams(val account: HGCAccountID) : IntentParams, UriConvertible, QRConvertible {
    var amount: Long = 0
    var note: String? = null
    var name: String? = null
    var notify: Boolean = false


    override fun asQRCode(): String {
        return asUri().toString()
    }

    override fun asUri(): Uri {
        return Uri.Builder().apply {
            scheme(IntentParams.APP_URL_SCHEMA)
            authority(IntentParams.APP_HOST)
            appendPath(IntentParams.APP_URL_PATH)
            appendQueryParameter("action", "payRequest")
            appendQueryParameter("acc", account.stringRepresentation())
            if (amount > 0)
                appendQueryParameter("a", "" + amount)

            if (!name.isNullOrEmpty())
                appendQueryParameter("name", name)

            if (!note.isNullOrEmpty())
                appendQueryParameter("n", note)

            if (notify)
                appendQueryParameter("nr", "1")

        }.build()

    }

    companion object {

        fun from(uri: Uri): TransferRequestParams? {
            return uri.getQueryParameter("action")?.let { action ->
                when (action) {
                    "payRequest" -> {
                        HGCAccountID.fromString(uri.getQueryParameter("acc"))?.let { account ->
                            TransferRequestParams(account).apply {
                                try {
                                    amount = java.lang.Long.parseLong(uri.getQueryParameter("a")!!)
                                } catch (e: Exception) {
                                }

                                name = uri.getQueryParameter("name")
                                note = uri.getQueryParameter("n")
                                notify = uri.getBooleanQueryParameter("nr", false)
                            }
                        }
                    }
                    else -> null
                }
            }
        }

        fun from(jsonObject: JSONObject?): TransferRequestParams? {
            return jsonObject?.let { json ->
                when (json.optString("action", "")) {
                    "payRequest" -> {
                        HGCAccountID.fromString(json.optString("acc"))?.let { accountID ->
                            TransferRequestParams(accountID).apply {
                                name = json.optString("name")
                                note = json.optString("n")
                                amount = json.optLong("a")
                            }
                        }
                    }
                    else -> null
                }
            }
        }

        fun fromBarCode(code: String): TransferRequestParams? {
            return Uri.parse(code)?.let { from(it) }
        }
    }
}
