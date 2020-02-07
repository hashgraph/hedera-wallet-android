package hedera.hgc.hgcwallet.view_controller.exchange_rate

import android.content.Context
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.model_controller.exchange.HbarTicker
import hedera.hgc.hgcwallet.view.exchange_rate.ExchangeRateView

class ExchangeRateScreen: Screen<ExchangeRateView>() {

    private val adapter = HbarPriceListAdapter()

    override fun createView(context: Context): ExchangeRateView {
        return ExchangeRateView(context, adapter)
    }

    override fun onResume(context: Context?) {
        super.onResume(context)
        HbarTicker.requestHbarPriceFromExchanges()
        // TODO: While this works as-is, there should be an observer for the exchange rate itself,
        //       which would alter tv_avg_exchange_rate.  Right now it 'works' by using the cached
        //       value which may not be fully updated before invocation.
    }
}