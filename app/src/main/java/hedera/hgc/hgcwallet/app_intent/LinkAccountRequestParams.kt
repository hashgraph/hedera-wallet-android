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

class LinkAccountRequestParams(val callback: Uri, val redirect: Uri?) {
    companion object {
        fun from(jsonObject: JSONObject?): LinkAccountRequestParams? {
            if (jsonObject != null) {
                val action = jsonObject.optString("action", "")
                if (action == "requestPublicKey") {
                    val callback = jsonObject.optString("callback", "")
                    val redirect = jsonObject.optString("redirect", "")
                    val callbackUri = Uri.parse(callback)
                    val redirectUri = Uri.parse(redirect)
                    if (callback != null) {
                        return LinkAccountRequestParams(callbackUri, redirectUri)
                    }
                }

            }
            return null
        }
    }
}










