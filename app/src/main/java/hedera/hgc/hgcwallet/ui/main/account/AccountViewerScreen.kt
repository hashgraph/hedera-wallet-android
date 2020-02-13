package hedera.hgc.hgcwallet.ui.main.account

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.TaskExecutor
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.database.account.AccountType
import hedera.hgc.hgcwallet.hapi.tasks.UpdateBalanceTaskAPI
import hedera.hgc.hgcwallet.ui.BaseActivity
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.unaryPlus
import io.reactivex.disposables.CompositeDisposable


class AccountViewerScreen : Screen<AccountViewerView>(), AddAccountListener {

    data class Params(var accounts: List<Account>)

    val params: Params

    private val disposables = CompositeDisposable()

    init {
        params = Params(listOf())
        updateAccounts()
    }


    internal fun onAddButtonClick() {
        navigator?.goTo(AddAccountScreen(null, this))
    }

    internal fun onAccountPick(account: Account) {
        navigator.goTo(AddAccountScreen(account, this))
    }

    override fun createView(context: Context): AccountViewerView {
        return AccountViewerView(context, params)
    }

    private fun fetchBalances(accounts: List<Account>) {
        (activity as? BaseActivity)?.showActivityProgress("Please wait")

        val taskExecutor = TaskExecutor().apply {
            setListner { task ->
                (activity as? BaseActivity)?.hideActivityProgress()
                task.error?.let { error ->
                    activity?.let { Singleton.showDefaultAlert(it, it.getString(R.string.failed_to_fetch_balances), error) }
                }

                view?.reload()
            }
        }

        taskExecutor.execute(UpdateBalanceTaskAPI(accounts))
    }

    override fun onAccountAdded(account: Account) {
        updateAccounts()
        navigator?.goBack()

    }

    override fun onAccountRemoved() {
        updateAccounts()
        navigator?.goBack()
    }

    private fun updateAccounts() {
        val accountList = DBHelper.getAllAccounts(AccountType.EXTERNAL).reversed()
        params.accounts = accountList
        fetchBalances(accountList)
        view?.reload()
    }

}

class AccountViewerView(context: Context, val params: AccountViewerScreen.Params) : BaseScreenView<AccountViewerScreen>(context) {
    private val recyclerView: RecyclerView

    init {
        View.inflate(context, R.layout.view_accout_list_layout, this)
        val titleBar = TitleBarWrapper(findViewById(R.id.titleBar))
        titleBar.setTitle("Account Viewer")
        titleBar.setCloseButtonHidden(false)
        titleBar.setImageResource(R.drawable.ic_add_account)
        titleBar.setOnCloseButtonClickListener { screen?.onAddButtonClick() }
        recyclerView = findViewById<View>(R.id.account_list_recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = AccountAdapter(params.accounts).apply {
            onAccountClick = object : AccountAdapter.OnItemClick {
                override fun onAccount(position: Int, account: Account) {
                    screen?.onAccountPick(account)
                }

                override fun onRefresh() {

                }
            }
        }
    }

    fun reload() {
        recyclerView.adapter?.notifyDataSetChanged()
    }
}

private class AccountAdapter(private val accountList: List<Account>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    inner class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnRefresh: Button

        init {
            btnRefresh = view.findViewById<Button>(R.id.btn_refresh)
        }
    }

    var onAccountClick: OnItemClick? = null


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {

        return when (p1) {
            VIEW_TYPES.Footer -> {
                val itemView = LayoutInflater.from(p0.getContext())
                        .inflate(R.layout.view_footer, p0, false)
                FooterViewHolder(itemView)
            }
            else -> {
                val itemView = LayoutInflater.from(p0.context).inflate(R.layout.account_list_row, p0, false)
                AccountViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FooterViewHolder -> {
                holder.btnRefresh.setOnClickListener {
                    onAccountClick?.onRefresh()
                }
            }
            else -> {
                val account = accountList[position]
                (holder as AccountViewHolder).setData(account)
                account.accountID()?.let { holder.key.text = it.stringRepresentation() }
                holder.itemView.setOnClickListener {
                    onAccountClick?.onAccount(position, account)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return accountList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (accountList.size == position)
            VIEW_TYPES.Footer;
        else
            VIEW_TYPES.Normal;
    }

    interface OnItemClick {
        fun onAccount(position: Int, account: Account)
        fun onRefresh()
    }

    private object VIEW_TYPES {
        val Header = 1
        val Normal = 2
        val Footer = 3
    }
}
