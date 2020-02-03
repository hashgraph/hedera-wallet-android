/*
 *
 *  Copyright 2019 Hedera Hashgraph LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package hedera.hgc.hgcwallet.ui.main.navigation_menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.modals.ExchangeRateInfo
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import java.util.*

class ExchangeRateScreen: Screen<ExchangeRateView>() {

    data class Params(val rateList: List<ExchangeRateInfo>)

    private val params = Params(Singleton.getAllRates())

    override fun createView(context: Context): ExchangeRateView {
        return ExchangeRateView(context, params)
    }
}

class ExchangeRateView(
        context: Context,
        val params: ExchangeRateScreen.Params
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
            adapter = ExchangeRateListAdapter(params.rateList)
        }

        findViewById<TextView>(R.id.tv_avg_exchange_rate)?.apply {
            val amount = Singleton.hgcToUSD(Singleton.toNanoCoins(1.0))
            val usd = Singleton.formatUSD(amount, true, maxFractionDigitCount = 99)
            text = "1 ${context.getString(R.string.text_hgc_currency_icon)} = $usd"
        }
    }
}

private class ExchangeRateListAdapter(
        private val exchangeRateList: List<ExchangeRateInfo>
):
        RecyclerView.Adapter<ExchangeInfoViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeInfoViewHolder {

        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.exchange_rate_row, parent, false)
        return ExchangeInfoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExchangeInfoViewHolder, position: Int) {

        holder.setData(exchangeRateList[position])
    }

    override fun getItemCount(): Int { return exchangeRateList.size }
}

private class ExchangeInfoViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val tvExchangeName: TextView = view.findViewById(R.id.text_exchange_name)
    val tvLast: TextView = view.findViewById(R.id.text_last)
    val tvBid: TextView = view.findViewById(R.id.text_bid)
    val tvAsk: TextView = view.findViewById(R.id.text_ask)
    val tvUpdatedTime: TextView = view.findViewById(R.id.text_time)

    fun setData(rateInfo: ExchangeRateInfo) {

        tvExchangeName.text = rateInfo.exchange.value
        val last = rateInfo.last ?: "--"
        val bid = rateInfo.bid ?: "--"
        val ask = rateInfo.ask ?: "--"
        tvLast.text = "${App.instance.getString(R.string.last)}: $last"
        tvBid.text = "${App.instance.getString(R.string.bid)}: $bid"
        tvAsk.text = "${App.instance.getString(R.string.ask)}: $ask"
        val updateTime = rateInfo.lastUpdated?.let {
                Singleton.getDateFormat(Date(it * 1000L))
            }
            ?: "--"
        tvUpdatedTime.text = App.instance.getString(R.string.text_last_updated, updateTime)
    }
}