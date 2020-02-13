package hedera.hgc.hgcwallet.crypto

interface KeyChain {
    fun keyAtIndex(index: Int): KeyPair
}