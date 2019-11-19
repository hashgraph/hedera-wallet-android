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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import java.util.ArrayList

import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class FeeScheduleScreen : Screen<FeeScheduleScreenView>() {
    override fun createView(context: Context): FeeScheduleScreenView {
        return FeeScheduleScreenView(context)
    }
}

class FeeScheduleScreenView(context: Context) : BaseScreenView<FeeScheduleScreen>(context) {

    init {
        View.inflate(context, R.layout.view_accout_list_layout, this)
        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setTitle("FEE SCHEDULE")
        }
        val mList = listOf("Balance Check", "Transaction History", "Make Payment")

        val mAdapter = FeeScheduleListAdapter(mList)
        findViewById<RecyclerView>(R.id.account_list_recyclerview)?.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = mAdapter
        }

    }
}

internal class FeeScheduleListAdapter(private val mlist: List<String>) : RecyclerView.Adapter<FeeScheduleListAdapter.MyViewHolder>() {


    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mFeeName: TextView
        val hgcWalletValue: TextView

        init {
            mFeeName = view.findViewById<View>(R.id.text_fee_name) as TextView
            hgcWalletValue = view.findViewById<View>(R.id.hgc_wallet_text) as TextView
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.fee_schedule_list_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.mFeeName.text = mlist[position]
    }

    override fun getItemCount(): Int {
        return mlist.size
    }
}