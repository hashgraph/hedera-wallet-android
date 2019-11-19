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
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.PublicKeyAddress
import org.json.JSONObject

class LinkAccountParams private constructor(
        val accountID: HGCAccountID,
        val address: PublicKeyAddress,
        val redirect: Uri
) {

    companion object {
        fun from(jsonObject: JSONObject?): LinkAccountParams? {
            var accountParams: LinkAccountParams? = null
            jsonObject?.let { obj ->
                when (obj.optString("action", "")) {
                    "recvAccountId", "setAccountId" -> {
                        PublicKeyAddress.from(obj.optString("publicKey", ""))?.let { pk ->
                            val realmNum = obj.optLong("realmNum", 0L)
                            val shardNum = obj.optLong("shardNum", 0L)
                            val accountNum = obj.optLong("accountNum", 0L)
                            val redirect = obj.optString("redirect", "")
                            val redirectUri = Uri.parse(redirect)
                            if (realmNum != 0L || shardNum != 0L || accountNum != 0L)
                                accountParams = LinkAccountParams(HGCAccountID(realmNum, shardNum, accountNum), pk, redirectUri)
                        }

                    }
                    else -> Unit
                }
            }
            return accountParams
        }
    }

}