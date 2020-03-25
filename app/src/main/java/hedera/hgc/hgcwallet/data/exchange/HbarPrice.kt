package hedera.hgc.hgcwallet.data.exchange

/// Canonized exchange information for the status of HBAR-USD trades.
data class HbarPrice(
        val exchange: Exchange,
        val date: Long? = null,
        var last: Double? = null,
        var bid: Double? = null,
        var ask: Double? = null
)
