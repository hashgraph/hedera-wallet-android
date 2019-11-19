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

package hedera.hgc.hgcwallet.database.contact

import androidx.room.Entity
import androidx.room.PrimaryKey
import hedera.hgc.hgcwallet.database.Converters


@Entity
data class Contact(@PrimaryKey var accountId: String, var name: String?, var isVerified: Boolean = false, var metaData: String = "") {

    fun isThirdPartyContact(): Boolean {
        val host = getHost()
        return (host != null && host.isNotBlank())
    }

    fun getHost(): String? {
        return if (metaData.isNotBlank()) {
            val metaDataMap = Converters().fromString(metaData)
            metaDataMap.get("host")
        } else null
    }
}
