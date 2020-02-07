package hedera.hgc.hgcwallet.model.exchange

import androidx.annotation.VisibleForTesting
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Protocol
import com.squareup.okhttp.Request
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.data.exchange.Exchange
import hedera.hgc.hgcwallet.data.exchange.HbarPrice
import org.json.JSONObject

/**
 * Model for exchange websites.
 *
 * An exchange provides a network interface for a RawHbarPrice.  It also provides a way to convert
 * one of its RawHbarPrice data classes into a canonized HbarPrice.
 */
object RawExchangeWebsite {

    /// A (blocking) network call for the latest RawHbarPrice information from an exchange.
    fun rawHbarPriceFrom(exchange: Exchange): RawHbarPrice {
        val url = urlFor(exchange)
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.protocols = listOf(Protocol.HTTP_1_1)
        val call = client.newCall(request)
        val response = call.execute()
        val result = response.body().string() ?: ""
        return RawHbarPrice(exchange, result, Singleton.getCurrentUTCSeconds())
    }

    /// Public API URLs for the supported exchanges.
    @VisibleForTesting
    internal fun urlFor(exchange: Exchange): String =
            when (exchange) {
                Exchange.Bittrex -> "https://api.bittrex.com/api/v1.1/public/getticker?market=USD-HBAR"
                Exchange.OKCoin -> "https://www.okcoin.com/api/spot/v3/instruments/HBAR-USD/ticker"
                Exchange.Liquid -> "https://api.liquid.com/products/557"
            }

    /// Logic for decoding each URL response body into an HbarPrice
    fun process(rawHbarPrice: RawHbarPrice): HbarPrice =
            try {
                val json = JSONObject(rawHbarPrice.data)
                val (last, bid, ask) = when (rawHbarPrice.exchange) {
                    Exchange.Bittrex -> json.optJSONObject("result")?.let { obj ->
                        readCommonInnerTickerData(obj, "Last", "Bid", "Ask")
                    } ?: run {
                        Triple(null, null, null)
                    }
                    Exchange.OKCoin ->
                        readCommonInnerTickerData(json, "last", "bid", "ask")
                    Exchange.Liquid ->
                        readCommonInnerTickerData(json, "last_traded_price",
                                "market_bid", "market_ask")
                }
                HbarPrice(rawHbarPrice.exchange, rawHbarPrice.date, last, bid, ask)
            } catch (e: Exception) {
                HbarPrice(rawHbarPrice.exchange, rawHbarPrice.date)
            }
}

//
// Helper functions for process()
//

private fun readOptionalDouble(obj: JSONObject, label: String): Double? =
        obj.optDouble(label, 0.0).let { if (it > 0.0) it else null }

private fun readCommonInnerTickerData(obj: JSONObject, last: String, bid: String, ask: String)
        : Triple<Double?, Double?, Double?>
        = Triple(
        readOptionalDouble(obj, last),
        readOptionalDouble(obj, bid),
        readOptionalDouble(obj, ask))