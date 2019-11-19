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

package hedera.hgc.hgcwallet.crypto

import hedera.hgc.hgcwallet.crypto.bip39.Mnemonic

class EDKeyChain(private val hgcSeed: HGCSeed) : KeyChain {

    override fun keyAtIndex(index: Int): KeyPair {
        val edSeed = CryptoUtils.deriveKey(hgcSeed.entropy, index.toLong(), 32)
        return EDKeyPair(edSeed)
    }
}

class EDBip32KeyChain(private val hgcSeed: HGCSeed) : KeyChain {
    override fun keyAtIndex(index: Int): KeyPair {
        val words = hgcSeed.toWordsList().joinToString(" ")
        val seed = Mnemonic.generateSeed(words, "")
        val ckd = SLIP10.deriveEd25519PrivateKey(seed, 44, 3030, 0, 0, index)
        return EDKeyPair(ckd)
    }
}