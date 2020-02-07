package hedera.hgc.hgcwallet.model.exchange

import hedera.hgc.hgcwallet.data.exchange.Exchange

/// The raw information from an exchange website containing HBAR price data.
data class RawHbarPrice(
        val exchange: Exchange,
        val data: String,
        val date: Long
)