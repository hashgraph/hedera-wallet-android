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

package hedera.hgc.hgcwallet.modals

data class ExchangeInfo(var accountId: HGCAccountID, var name: String, var host: String, var memo: String?) {

    companion object {

        fun fromQRCode(code: String): ExchangeInfo? {
            val components = code.split(",")
            return if (components.size > 3) {
                val memo = if (components.size > 4) components[4] else null
                HGCAccountID.fromString(components[3])?.let {
                    ExchangeInfo(it, components[0], "${components[1]}:${components[2]}", memo)
                }

            } else
                null
        }

        fun toHttpUrl(host: String): String {
            return if (host.startsWith("http://", true) || host.startsWith("https://", true))
                host
            else
                "http://$host"
        }
    }
}