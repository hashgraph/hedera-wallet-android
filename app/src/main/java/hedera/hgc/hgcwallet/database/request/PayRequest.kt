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

package hedera.hgc.hgcwallet.database.request

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class PayRequest(
        @PrimaryKey(autoGenerate = true) var requestId: Long,
        var accountId: String,
        var name: String?,
        var notes: String?,
        var amount: Long = 0,
        var importDate: Date
)