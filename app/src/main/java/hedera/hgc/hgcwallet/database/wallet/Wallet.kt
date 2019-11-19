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

package hedera.hgc.hgcwallet.database.wallet

import androidx.room.Entity
import androidx.room.PrimaryKey
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.common.UserSettings

import hedera.hgc.hgcwallet.modals.HGCKeyType
import hedera.hgc.hgcwallet.modals.KeyDerivation

@Entity
class Wallet(
        @PrimaryKey(autoGenerate = true)
        var walletId: Long = 0,
        var totalAccounts: Long = 0,
        var keyType: String = "",
        var keyDerivationType: String = ""
) {
    constructor() : this(0, 0, "", "")


    fun getHGCKeyType(): HGCKeyType {
        return Wallet.keyTypeFrom(keyType)
    }

    fun setHGCKeyDerivationType(kd: KeyDerivation) {
        keyDerivationType = when (kd) {
            KeyDerivation.BIP32 -> "bip32"
            else -> ""
        }
    }

    fun getHGCKeyDerivationType(): KeyDerivation {
        return when (keyDerivationType) {
            "bip32" -> KeyDerivation.BIP32
            else -> KeyDerivation.HGC
        }
    }

    companion object {

        fun createWallet(type: HGCKeyType, keyDerivation: KeyDerivation): Wallet {
            val type = when (type) {
                HGCKeyType.ED25519 -> "ED25519"
                HGCKeyType.RSA3072 -> "RSA3072"
                HGCKeyType.ECDSA384 -> "ECDSA384"
            }
            return Wallet(0, 0, type, "").apply { setHGCKeyDerivationType(keyDerivation) }
        }

        fun clear(){
            // Clear DB
            App.instance.database?.clearDatabase()

            // Clear Shared Pref
            UserSettings.clear()

            // Rebuild Nodes
            App.instance.createAddressBook()

        }
        fun keyTypeFrom(keyType: String): HGCKeyType {
            when (keyType) {
                "ED25519" -> return HGCKeyType.ED25519

                "RSA3072" -> return HGCKeyType.RSA3072

                "ECDSA384" -> return HGCKeyType.ECDSA384
            }
            return HGCKeyType.ECDSA384
        }

    }
}
