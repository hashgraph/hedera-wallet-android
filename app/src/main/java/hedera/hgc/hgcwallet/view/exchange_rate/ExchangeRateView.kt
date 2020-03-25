package hedera.hgc.hgcwallet.view.exchange_rate

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wealthfront.magellan.BaseScreenView
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.view_controller.exchange_rate.ExchangeRateScreen

// todo: see IntelliJ note
class ExchangeRateView(
        context: Context,
        adapter: RecyclerView.Adapter<HbarPriceViewHolder>
):
        BaseScreenView<ExchangeRateScreen>(context)
{
    init {
        View.inflate(context, R.layout.view_exchange_rate_layout, this)
        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setCloseButtonHidden(true)
            setTitle(R.string.menuItem_exchange_rate)
        }

        findViewById<RecyclerView>(R.id.exchange_list)?.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            setAdapter(adapter)
        }

        findViewById<TextView>(R.id.tv_avg_exchange_rate)?.apply {
            val amount = Singleton.hgcToUSD(Singleton.toNanoCoins(1.0))
            val usd = Singleton.formatUSD(amount, true, maxFractionDigitCount = 99)
            text = "1 ${context.getString(R.string.text_hgc_currency_icon)} = $usd"
        }
    }
}