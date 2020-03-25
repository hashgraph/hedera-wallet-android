package hedera.hgc.hgcwallet.data.exchange

import hedera.hgc.hgcwallet.Config

/// An business that coordinates trades between HBAR and USD.
enum class Exchange {
    Bittrex,
    Liquid,
    OKCoin;

    val displayName: String
        get() {
            return when (this) {
                Bittrex -> "Bittrex"
                OKCoin -> "OKCoin"
                Liquid -> "Liquid"
            }
        }

    val id: Int
        get() {
            return when (this) {
                Bittrex -> 0
                Liquid -> 1
                OKCoin -> 2
            }
        }
}