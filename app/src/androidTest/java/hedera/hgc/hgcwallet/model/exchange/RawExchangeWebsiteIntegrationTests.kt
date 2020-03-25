package hedera.hgc.hgcwallet.model.exchange

import android.webkit.URLUtil
import androidx.test.runner.AndroidJUnit4
import hedera.hgc.hgcwallet.data.exchange.Exchange
import hedera.hgc.hgcwallet.data.exchange.HbarPrice
import hedera.hgc.hgcwallet.model.exchange.RawExchangeWebsite
import hedera.hgc.hgcwallet.model.exchange.RawHbarPrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RawExchangeWebsiteIntegrationTests {

    private val bittrexExampleResponse = "{\"success\":true,\"message\":\"\",\"result\":{" +
            "\"Bid\":0.01930000,\"Ask\":0.01997856,\"Last\":0.01964249}}"
    private val bittrexExampleRawHbarPrice =
            RawHbarPrice(Exchange.Bittrex, bittrexExampleResponse, 0L)
    private val bittrexExampleHbarPrice =
            HbarPrice(Exchange.Bittrex, 0L, 0.01964249, 0.01930000, 0.01997856)

    private val liquidExampleResponse = "{\"id\":\"557\",\"product_type\":\"CurrencyPair\"," +
            "\"code\":\"CASH\",\"name\":null,\"market_ask\":0.038,\"market_bid\":0.0169," +
            "\"indicator\":null,\"currency\":\"USD\",\"currency_pair_code\":\"HBARUSD\"," +
            "\"symbol\":null,\"btc_minimum_withdraw\":null,\"fiat_minimum_withdraw\":null," +
            "\"pusher_channel\":\"product_cash_hbarusd_557\",\"taker_fee\":\"0.001\"," +
            "\"maker_fee\":\"0.001\",\"low_market_bid\":\"0.0169\"," +
            "\"high_market_ask\":\"0.038\",\"volume_24h\":\"122720.1960539\"," +
            "\"last_price_24h\":\"0.01691\",\"last_traded_price\":\"0.038\"," +
            "\"last_traded_quantity\":\"2.81434679\",\"quoted_currency\":\"USD\"," +
            "\"base_currency\":\"HBAR\",\"tick_size\":\"0.00001\",\"disabled\":true," +
            "\"margin_enabled\":false,\"cfd_enabled\":false,\"perpetual_enabled\":false," +
            "\"last_event_timestamp\":\"1580927820.079045971\"," +
            "\"timestamp\":\"1580927820.079045971\",\"exchange_rate\":0}"
    private val liquidExampleRawHbarPrice =
            RawHbarPrice(Exchange.Liquid, liquidExampleResponse, 0L)
    private val liquidExampleHbarPrice =
            HbarPrice(Exchange.Liquid, 0L, 0.038, 0.0169, 0.038)

    private val okcoinExampleResponse = "{\"best_ask\":\"0.0208\",\"best_bid\":\"0.0205\"," +
            "\"instrument_id\":\"HBAR-USD\",\"product_id\":\"HBAR-USD\",\"last\":\"0.0195\"," +
            "\"last_qty\":\"0\",\"ask\":\"0.0208\",\"best_ask_size\":\"47033.4509\"," +
            "\"bid\":\"0.0205\",\"best_bid_size\":\"60336.9972\",\"open_24h\":\"0.0186\"," +
            "\"high_24h\":\"0.0201\",\"low_24h\":\"0.017\",\"base_volume_24h\":\"228709.3143\"," +
            "\"timestamp\":\"2020-02-05T18:44:05.710Z\",\"quote_volume_24h\":\"4031.9526\"}"
    private val okcoinExampleRawHbarPrice =
            RawHbarPrice(Exchange.OKCoin, okcoinExampleResponse, 0L)
    private val okcoinExampleHbarPrice =
            HbarPrice(Exchange.OKCoin, 0L, 0.0195, 0.0205, 0.0208)

    // Note: this is actually a unit test, but URLUtil's use requires the Android runtime.
    @Test
    fun urlForExchange() {
        for (exchange in Exchange.values()) {
            val url = RawExchangeWebsite.urlFor(exchange)
            assertTrue(URLUtil.isValidUrl(url))
            assertTrue(URLUtil.isHttpsUrl(url))
        }
    }

    @Test
    fun process() {
        // Integration with JSON parser.
        assertEquals(bittrexExampleHbarPrice, RawExchangeWebsite.process(bittrexExampleRawHbarPrice))
        assertEquals(liquidExampleHbarPrice, RawExchangeWebsite.process(liquidExampleRawHbarPrice))
        assertEquals(okcoinExampleHbarPrice, RawExchangeWebsite.process(okcoinExampleRawHbarPrice))
    }

    // TODO: Unit test for rawHbarPriceFrom(exchange) against mocked network.

    // TODO: System test for rawHbarPriceFrom(exchange).
}