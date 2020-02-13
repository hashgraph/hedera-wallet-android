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