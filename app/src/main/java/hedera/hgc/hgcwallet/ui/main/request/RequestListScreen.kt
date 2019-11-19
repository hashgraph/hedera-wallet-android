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

package hedera.hgc.hgcwallet.ui.main.request


import android.content.Context
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.database.request.PayRequest
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.ui.main.pay.PayScreen
import hedera.hgc.hgcwallet.ui.main.pay.PayType

class RequestListScreen : Screen<RequestListView>() {

    data class Params(val requestList: List<PayRequest>, val accountList: List<Account>)

    private val params = Params(DBHelper.getAllRequests() ?: listOf(), DBHelper.getAllAccounts())

    override fun createView(context: Context): RequestListView {
        return RequestListView(context, params)
    }

    internal fun onPayRequestForAccount(payRequest: PayRequest) {
        val fromAccount = params.accountList.first()
        navigator?.goTo(PayScreen(fromAccount, payRequest, PayType.Pay))
    }
}

class RequestListView(context: Context, val params: RequestListScreen.Params) : BaseScreenView<RequestListScreen>(context) {

    private var mRecyclerView: RecyclerView? = null

    init {
        View.inflate(context, R.layout.view_accout_list_layout, this)
        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setTitle("REQUESTS")
        }

        if (params.requestList.isNotEmpty()) {
            val mAdapter = RequestListAdapter(params.requestList, object : RequestListAdapter.OnPayRequestClick {
                override fun onRequestPick(payRequest: PayRequest) {
                    // below code should be discuss
                    screen?.onPayRequestForAccount(payRequest)
                }
            })

            mRecyclerView = findViewById<RecyclerView>(R.id.account_list_recyclerview)?.apply {
                layoutManager = LinearLayoutManager(context)
                itemAnimator = DefaultItemAnimator()
                setHasFixedSize(true)
                adapter = mAdapter
            }
        }
    }
}

internal class RequestListAdapter(private val requestList: List<PayRequest>, private val requestListener: OnPayRequestClick?) : RecyclerView.Adapter<RequestListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.request_list_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val payRequest = requestList[position]
        holder.mTextViewAccountName.visibility = View.VISIBLE

        holder.mTextViewAccountName.text = if (!payRequest.name.isNullOrEmpty()) payRequest.name else "UNKNOWN"

        if (!payRequest.notes.isNullOrEmpty()) {
            holder.mTextViewNotes.text = payRequest.notes
            holder.mTextViewNotes.visibility = View.VISIBLE
        } else {
            holder.mTextViewNotes.visibility = View.GONE
        }

        if (payRequest.accountId.isNotEmpty()) {
            holder.mTextViewAccountId.text = payRequest.accountId
            holder.mTextViewAccountId.visibility = View.VISIBLE
        } else {
            holder.mTextViewAccountId.visibility = View.GONE
        }


        holder.mTextViewTime.text = Singleton.getDateFormat(payRequest.importDate)
        holder.mTextViewTime.visibility = View.VISIBLE


        if (payRequest.amount > 0) {
            holder.mTextViewDollerValue.text = Singleton.formatUSD(payRequest.amount, true)
            holder.mTextViewHgcWalletValue.text = Singleton.formatHGCShort(payRequest.amount, true)
            holder.mTextViewDollerValue.visibility = View.VISIBLE
            holder.mTextViewHgcWalletValue.visibility = View.VISIBLE
        } else {
            holder.mTextViewDollerValue.visibility = View.INVISIBLE
            holder.mTextViewHgcWalletValue.visibility = View.INVISIBLE
        }

        holder.mButtonPay.visibility = View.VISIBLE

        holder.mButtonPay.setOnClickListener {
            requestListener?.onRequestPick(payRequest)
        }
    }


    override fun getItemCount(): Int {
        return requestList.size
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mTextViewTime: TextView
        var mTextViewAccountName: TextView
        var mTextViewHgcWalletValue: TextView
        var mTextViewAccountId: TextView
        var mTextViewDollerValue: TextView
        var mTextViewNotes: TextView
        var mButtonPay: Button

        init {
            mTextViewTime = view.findViewById<View>(R.id.text_time) as TextView
            mTextViewAccountName = view.findViewById<View>(R.id.text_account_name) as TextView
            mTextViewHgcWalletValue = view.findViewById<View>(R.id.hgc_wallet_text) as TextView
            mTextViewAccountId = view.findViewById<View>(R.id.text_key) as TextView
            mTextViewDollerValue = view.findViewById<View>(R.id.dollor_text) as TextView
            mTextViewNotes = view.findViewById<View>(R.id.notes_text) as TextView
            mButtonPay = view.findViewById<View>(R.id.btn_pay) as Button
        }
    }

    interface OnPayRequestClick {
        fun onRequestPick(payRequest: PayRequest)
    }
}
