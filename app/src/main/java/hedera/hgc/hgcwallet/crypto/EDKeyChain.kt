

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
