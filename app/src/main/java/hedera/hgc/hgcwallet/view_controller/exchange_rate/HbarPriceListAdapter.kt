package hedera.hgc.hgcwallet.view_controller.exchange_rate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.data.exchange.Exchange
import hedera.hgc.hgcwallet.data.exchange.HbarPrice
import hedera.hgc.hgcwallet.model_controller.exchange.HbarTicker
import hedera.hgc.hgcwallet.view.exchange_rate.HbarPriceViewHolder
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class HbarPriceListAdapter: RecyclerView.Adapter<HbarPriceViewHolder>() {

    private var list: MutableList<HbarPrice> = LinkedList(Exchange.values().map{ exchange -> HbarPrice(exchange) })
    private var subscriptions = {
        val subscriptions = CompositeDisposable()
        for (exchange in Exchange.values()) {
            val subscription = HbarTicker.forExchange(exchange).subscribe { newPrice ->
                val dataId = newPrice.exchange.id
                val item = list.find { it.exchange.id == dataId }
                if (item != null) {
                    val oldIndex = list.indexOf(item)
                    list[oldIndex] = newPrice
                    notifyItemChanged(oldIndex)
                } else {
                    list.add(newPrice)
                    notifyItemInserted(list.indexOf(list.last()))
                }
            }
            subscriptions.add(subscription)
        }
        subscriptions
    }()

    init {
        super.setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HbarPriceViewHolder {

        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.exchange_rate_row, parent, false)
        return HbarPriceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HbarPriceViewHolder, position: Int) {

        holder.setData(list[position])
    }

    override fun getItemCount(): Int { return list.size }

    override fun getItemId(position: Int): Long {
        return list[position].exchange.id.toLong()
    }
}