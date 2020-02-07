package hedera.hgc.hgcwallet.model.exchange

import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.data.exchange.Exchange

fun UserSettings.setRawTickerCacheData(rawHbarPrice: RawHbarPrice) {

    val (dataKey, dateKey) = getExchangeKeys(rawHbarPrice.exchange)
    setValue(dataKey, rawHbarPrice.data)
    setValue(dateKey, rawHbarPrice.date)
}

fun UserSettings.getRawTickerCacheData(exchange: Exchange): RawHbarPrice {

    val (dataKey, dateKey) = getExchangeKeys(exchange)
    val data = getValue(dataKey)
    val date = getLongValue(dateKey)
    return if (data != null && date > 0)
        RawHbarPrice(exchange, data, date)
    else
        RawHbarPrice(exchange, "", 0L)
}

private fun UserSettings.getExchangeKeys(exchange: Exchange): Pair<String, String> =
    when (exchange) {
        Exchange.Bittrex -> KEY_BITTREX_EXCHANGE_RATE_DATA to KEY_BITTREX_EXCHANGE_RATE_DATE
        Exchange.OKCoin -> KEY_OKCOIN_EXCHANGE_RATE_DATA to KEY_OKCOIN_EXCHANGE_RATE_DATE
        Exchange.Liquid -> KEY_LIQUID_EXCHANGE_RATE_DATA to KEY_LIQUID_EXCHANGE_RATE_DATE
    }
