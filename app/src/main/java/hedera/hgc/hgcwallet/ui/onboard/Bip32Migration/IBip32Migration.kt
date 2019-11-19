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

package hedera.hgc.hgcwallet.ui.onboard.Bip32Migration

import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.crypto.KeyPair
import hedera.hgc.hgcwallet.modals.HGCAccountID

interface IBip32Migration {
    fun getOldKey(): KeyPair
    fun getAccountID(): HGCAccountID
    fun bip32MigrationAborted()
    fun bip32MigrationRetry()
    fun bip32MigrationSuccessful(newSeed: HGCSeed, accountID: HGCAccountID)
}