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

package hedera.hgc.hgcwallet.ui.main.account

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.account.Account

class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var accountName: TextView
    var hgcWalletValue: TextView
    var key: TextView
    var dollarValue: TextView

    init {
        accountName = view.findViewById<View>(R.id.text_account_name) as TextView
        hgcWalletValue = view.findViewById<View>(R.id.hgc_wallet_text) as TextView
        key = view.findViewById<View>(R.id.text_key) as TextView
        dollarValue = view.findViewById<View>(R.id.dollor_text) as TextView
    }

    fun setData(account: Account) {

        accountName.text = if (account.name.isBlank()) App.instance.getString(R.string.unknown) else account.name
        val shortKey = Singleton.publicKeyStringShort(account)
        key.text = App.instance.getString(R.string.text_key_short, shortKey)
        val nanoCoins = account.balance
        hgcWalletValue.text = Singleton.formatHGCShort(nanoCoins, true)
        dollarValue.text = Singleton.formatUSD(nanoCoins, true)
    }
}