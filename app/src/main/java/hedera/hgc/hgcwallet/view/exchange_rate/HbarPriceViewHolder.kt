package hedera.hgc.hgcwallet.view.exchange_rate

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.data.exchange.HbarPrice
import java.util.*

class HbarPriceViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val tvExchangeName: TextView = view.findViewById(R.id.text_exchange_name)
    val tvLast: TextView = view.findViewById(R.id.text_last)
    val tvBid: TextView = view.findViewById(R.id.text_bid)
    val tvAsk: TextView = view.findViewById(R.id.text_ask)
    val tvTextTime: TextView = view.findViewById(R.id.text_time)

    fun setData(hbarPrice: HbarPrice) {

        tvExchangeName.text = hbarPrice.exchange.displayName
        val last = hbarPrice.last ?: "--"
        val bid = hbarPrice.bid ?: "--"
        val ask = hbarPrice.ask ?: "--"
        tvLast.text = "${App.instance.getString(R.string.last)}: $last"
        tvBid.text = "${App.instance.getString(R.string.bid)}: $bid"
        tvAsk.text = "${App.instance.getString(R.string.ask)}: $ask"
        val updateTime = hbarPrice.date?.let { date ->
            Singleton.getDateFormat(Date(date * 1000L))
        }
                ?: "--"
        tvTextTime.text = App.instance.getString(R.string.text_last_updated, updateTime)
    }
}