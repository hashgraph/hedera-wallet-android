package hedera.hgc.hgcwallet.ui.main.account


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

import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class AccountListScreen(private val pickerListener: AccountPickerListener?) : Screen<AccountListView>() {

    data class Params(val accounts: List<Account>, val hasPickerListener: Boolean)

    val params = Params(DBHelper.getAllAccounts(), pickerListener != null)

    interface AccountPickerListener {
        fun onAccountPick(account: Account)
    }

    override fun createView(context: Context): AccountListView {
        return AccountListView(context, params)
    }

    internal fun onAccountPick(account: Account) {
        if (pickerListener == null)
            navigator.goTo(AccountCreateScreen(account, "ACCOUNT DETAILS", true))
        else
            pickerListener.onAccountPick(account)

    }
}

class AccountListView(context: Context, val params: AccountListScreen.Params) : BaseScreenView<AccountListScreen>(context) {

    private val recyclerView: RecyclerView

    init {
        View.inflate(context, R.layout.view_accout_list_layout, this)
        val titleBar = TitleBarWrapper(findViewById(R.id.titleBar))

        val totalAccount = params.accounts.size
        titleBar.setCloseButtonHidden(true)
        val title = if (params.hasPickerListener)
            "Please select an account"
        else
            totalAccount.toString() + " ACTIVE ACCOUNT" + if (totalAccount == 1) "" else "S"
        titleBar.setTitle(title)


        recyclerView = findViewById<View>(R.id.account_list_recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = AccountListAdapter(params.accounts).apply {
            onAccountClick = object : AccountListAdapter.OnItemClick {
                override fun onAccount(position: Int, account: Account) {
                    screen?.onAccountPick(account)
                }
            }
        }
    }
}

private class AccountListAdapter(private val accountList: List<Account>) : RecyclerView.Adapter<AccountViewHolder>() {

    var onAccountClick: OnItemClick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.account_list_row, parent, false)

        return AccountViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accountList[position]
        holder.setData(account)

        holder.itemView.setOnClickListener {
            onAccountClick?.onAccount(position, account)
        }
    }

    override fun getItemCount(): Int {
        return accountList.size
    }


    interface OnItemClick {
        fun onAccount(position: Int, account: Account)
    }
}
