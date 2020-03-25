package hedera.hgc.hgcwallet.data.exchange

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExchangeUnitTests {

    private val bittrexDisplayName = "Bittrex"
    private val liquidDisplayName = "Liquid"
    private val okcoinDisplayName = "OKCoin"

    @Test
    fun exchangeDetails() {

        assertTrue(Exchange.values().isNotEmpty())

        assertEquals(bittrexDisplayName, Exchange.Bittrex.displayName)
        assertEquals(liquidDisplayName, Exchange.Liquid.displayName)
        assertEquals(okcoinDisplayName, Exchange.OKCoin.displayName)

        val ids = Array(Exchange.values().size) { false }
        for (exchange in Exchange.values()) {
            try {
                ids[exchange.id] = true
            } catch (e: IndexOutOfBoundsException) {
                assert(false)
            }
        }
        assertTrue(ids.reduce{a, b -> a && b})
    }
}