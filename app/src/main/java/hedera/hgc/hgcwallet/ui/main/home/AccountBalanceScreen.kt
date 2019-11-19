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

package hedera.hgc.hgcwallet.ui.main.home

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.app_intent.CreateAccountRequestParams
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.database.transaction.TxnRecord
import hedera.hgc.hgcwallet.local_auth.AuthListener
import hedera.hgc.hgcwallet.ui.auth.AuthActivity
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.ui.main.pay.PayScreen
import hedera.hgc.hgcwallet.ui.main.pay.PayType
import hedera.hgc.hgcwallet.ui.main.request.RequestScreen
import hedera.hgc.hgcwallet.ui.main.transcation.TransactionDetailsScreen
import hedera.hgc.hgcwallet.ui.onboard.Bip32Migration.Bip32MigrationActivity
import hedera.hgc.hgcwallet.ui.scan.QRPreviewScreen
import hedera.hgc.hgcwallet.unaryPlus
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class AccountBalanceScreen : Screen<AccountBalanceView>(), AuthListener {

    data class Params(var accountList: List<Account>, var txnList: List<TxnRecord>)

    private val param: Params
    private val disposables = CompositeDisposable()

    init {
        param = Params(listOf(), listOf())
        disposables.add((+DBHelper.getAllAccountsFlowable()).subscribe {
            param.accountList = it
            view?.reload()
        })
    }

    override fun createView(context: Context): AccountBalanceView {
        return AccountBalanceView(context, param)
    }

    override fun onAuthSetupSuccess() {}

    override fun onAuthSuccess(requestCode: Int) {
        if (requestCode == 100)
            view?.showPrivateKey()
    }

    override fun onAuthSetupFailed(isCancelled: Boolean) {}

    override fun onAuthFailed(requestCode: Int, isCancelled: Boolean) {}


    internal fun onPayButtonClick(account: Account) {
        navigator?.goTo(PayScreen(account, PayType.Pay))
    }

    internal fun onRequestButtonClick(account: Account) {
        navigator?.goTo(RequestScreen(account));
    }

    internal fun onTransactionClick(txn: TxnRecord) {
        navigator?.goTo(TransactionDetailsScreen(txn))
    }

    internal fun onRequestCreateAccountButtonClick(account: Account) {
        account.getPublicKeyAddres()?.let { publicKeyAddress ->
            val params = CreateAccountRequestParams.from(publicKeyAddress, null)
            navigator?.goTo(QRPreviewScreen(params.asQRCode()))
        }
    }

    internal fun getTransactionRecords(account: Account?) {
        disposables.add((+Single.fromCallable {
            // For now fetch all record regardless of account
            DBHelper.getAllTxnRecord(null)
        }).subscribe({
            param.txnList = it
            view?.updateTransactionList()
        }, {

        }))
    }

    internal fun updateAccountName(account: Account, name: String) {
        account.name = name
        DBHelper.saveAccount(account)
    }

    internal fun onAppWarningViewClick() {
        activity?.run {
            Singleton.getMasterAccountID()?.let { Bip32MigrationActivity.startActivity(this) }
                    ?: Singleton.showToast(activity, resources.getString(R.string.text_account_not_linked))
        }

    }
}

class AccountBalanceView(context: Context, val param: AccountBalanceScreen.Params) : BaseScreenView<AccountBalanceScreen>(context) {

    private val mRecyclerView: RecyclerView?
    private val mAdapter: TransactionListAdapter
    private val mToggleLayout: LinearLayout?
    private val mAccountDetailsView: RelativeLayout?
    private val mArrowDown: ImageView?
    private val mArrowUp: ImageView?
    private val mCustomAdapter: OverViewPagerAdapter
    private val mEditTextNickName: EditText?
    private val mTextViewPublicKey: TextView?
    private val mTextViewPrivateKey: TextView?
    private val mTextViewDisplayText: TextView?
    private val mTextViewHideText: TextView?
    private val mCopyImageView: ImageView?
    private val mCopyImageViewAccountData: ImageView?
    private val mTextViewNoTransaction: TextView?
    private var isPrivateKeyDisplay = false
    private var currentAccount: Account? = null
    private val viewPager: ViewPager

    init {
        View.inflate(context, R.layout.view_home_layout, this)
        mToggleLayout = findViewById<LinearLayout>(R.id.toggle_linear_layout)
        mAccountDetailsView = findViewById<RelativeLayout>(R.id.user_layout)
        mTextViewPublicKey = findViewById<TextView>(R.id.textview_public_address)
        mTextViewPrivateKey = findViewById<TextView>(R.id.textview_private_key)
        mTextViewNoTransaction = findViewById<TextView>(R.id.text_no_transaction)


        mCustomAdapter = OverViewPagerAdapter(context, param.accountList, object : OverViewPagerAdapter.OverViewPagerAdapterListener {
            override fun onPayButtonClick(account: Account) {
                screen?.onPayButtonClick(account)
            }

            override fun onRequestButtonClick(account: Account) {
                screen?.onRequestButtonClick(account)
            }

            override fun onRequestCreateAccountButtonClick(account: Account) {
                screen?.onRequestCreateAccountButtonClick(account)
            }

        })

        val bip39View = findViewById<TextView>(R.id.bip39_warning_tv)
        if (Singleton.canDoBip32Migration()) {
            Singleton.getMasterAccount()?.accountID()?.let { bip39View.text = App.instance.getString(R.string.bip32_migration_warning_message_purple, it.stringRepresentation()) }
            bip39View.setOnClickListener { screen?.onAppWarningViewClick() }
        } else
            bip39View.visibility = View.GONE


        mRecyclerView = findViewById<RecyclerView>(R.id.recyclerview)?.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
        }

        mAdapter = TransactionListAdapter().apply {
            setOnTransactionClick(object : TransactionListAdapter.OnTransactionClick {
                override fun onTransaction(txnRecord: TxnRecord) {
                    screen?.onTransactionClick(txnRecord)
                }
            })
        }

        mEditTextNickName = findViewById<EditText>(R.id.edittext_nick_name)?.apply {

            addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                }

                override fun afterTextChanged(editable: Editable) {
                    currentAccount?.let { if (it.name != text.toString()) screen?.updateAccountName(it, text.toString()) }
                }
            })
        }

        mArrowDown = findViewById<ImageView>(R.id.arrow_down)?.apply {
            setOnClickListener {
                mToggleLayout?.visibility = View.GONE
                currentAccount?.let {
                    mEditTextNickName?.setText(it.name)
                    Singleton.publicKeyString(it).let {
                        if (it.isNotEmpty())
                            mTextViewPublicKey?.text = it
                    }
                }
                mAccountDetailsView?.apply {
                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down))
                    visibility = View.VISIBLE
                    bringToFront()
                }
            }
        }
        mArrowUp = findViewById<ImageView>(R.id.arrow_up)?.apply {
            setOnClickListener {
                mToggleLayout?.visibility = View.VISIBLE
                mAccountDetailsView?.apply {
                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up))
                    visibility = View.INVISIBLE
                }
            }
        }

        mCopyImageView = findViewById<ImageView>(R.id.image_copy)?.apply {
            setOnClickListener {
                currentAccount?.let {
                    Singleton.publicKeyString(it).let {
                        if (it.isNotEmpty()) {
                            Singleton.copyToClipBoard(it, context)
                            (context as? Activity)?.let {
                                Singleton.showToast(it, context.getString(R.string.copy_data_clipboard_message))
                            }
                        }
                    }
                }
            }
        }

        mCopyImageViewAccountData = findViewById<ImageView>(R.id.image_copy_account_data)?.apply {
            setOnClickListener {
                currentAccount?.let { account ->
                    Singleton.accountToJSONString(account, isPrivateKeyDisplay)?.let {
                        Singleton.copyToClipBoard(it, context)
                        (context as? Activity)?.let {
                            Singleton.showToast(it, context.getString(R.string.copy_data_clipboard_account_data))
                        }
                    }
                }
            }
        }

        mTextViewDisplayText = findViewById<TextView>(R.id.text_display)?.apply {
            setOnClickListener {
                AlertDialog.Builder(context).apply {
                    setTitle("Warning")
                    setMessage("There is some risk involved in showing the private key.\n\nDo you really want to see it?")
                    setPositiveButton("No", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            p0?.dismiss()
                        }
                    })

                    setNegativeButton("Yes", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            p0?.dismiss()
                            (context as? AuthActivity)?.requestAuth(100)
                        }
                    })
                }.show()
            }
        }

        mTextViewHideText = findViewById<TextView>(R.id.text_hide)?.apply {
            setOnClickListener { hidePrivateKey() }
        }

        viewPager = findViewById<ViewPager>(R.id.viewpager)
        viewPager?.let { viewPager ->
            if (param.accountList.size > 1)
                findViewById<TabLayout>(R.id.dots)?.apply {
                    setupWithViewPager(viewPager, true)
                }
            viewPager.adapter = mCustomAdapter
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                var first = true
                override fun onPageScrollStateChanged(p0: Int) {}

                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                    if (first && p1 == 0f && p2 == 0 && p0 == 0) {
                        onPageSelected(0)
                        first = false
                    }
                }

                override fun onPageSelected(p0: Int) {
                    mRecyclerView?.setAdapter(mAdapter)
                    currentAccount = param.accountList.get(p0)
                    mAccountDetailsView?.visibility = View.INVISIBLE
                    mToggleLayout?.visibility = View.VISIBLE
                    hidePrivateKey()
                    screen?.getTransactionRecords(currentAccount)
                }
            })
        }
    }

    fun reload() {
        mCustomAdapter.accountsList = param.accountList
        viewPager.adapter = mCustomAdapter
    }

    fun updateTransactionList() {
        param.txnList.let {
            mAdapter.setList(it)
            mTextViewNoTransaction?.visibility = if (it.isNotEmpty()) View.INVISIBLE else View.VISIBLE
        }
    }

    fun showPrivateKey() {
        isPrivateKeyDisplay = true
        currentAccount?.let {
            val privateKeyString = Singleton.privateKeyString(it)
            mTextViewDisplayText?.visibility = View.GONE
            mTextViewHideText?.visibility = View.VISIBLE
            mTextViewPrivateKey?.setText(privateKeyString)
        }
    }

    fun hidePrivateKey() {
        isPrivateKeyDisplay = false
        mTextViewDisplayText?.visibility = View.VISIBLE
        mTextViewHideText?.visibility = View.GONE
        mTextViewPrivateKey?.setText(R.string.default_private_key)
    }

    internal class OverViewPagerAdapter(private val context: Context, var accountsList: List<Account>, private val listener: OverViewPagerAdapterListener? = null) : PagerAdapter() {
        private val inflater: LayoutInflater
        private var btnLayoutWithAccountId: LinearLayout? = null
        private var btnLayoutWithoutAccountId: LinearLayout? = null

        init {
            inflater = LayoutInflater.from(context)
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getCount(): Int {
            return accountsList.size
        }

        override fun instantiateItem(view: ViewGroup, position: Int): Any {
            val account = accountsList[position]
            val mView = inflater.inflate(R.layout.home_account_view, view, false)
            btnLayoutWithAccountId = mView.findViewById<LinearLayout>(R.id.layout_btn_account_id)
            btnLayoutWithoutAccountId = mView.findViewById<LinearLayout>(R.id.layout_btn_no_account_id)
            val lastUpdatedText = mView.findViewById<TextView>(R.id.last_check_date)

            TitleBarWrapper(mView.findViewById<View>(R.id.titleBar)).apply {
                setTitle("${account.name}...${Singleton.publicKeyStringShort(account)}")
            }

            var nanoCoins: Long = 0
            when (account) {
                null -> {
                    nanoCoins = Singleton.getTotalBalance()
                    lastUpdatedText.text = ""
                }
                else -> {
                    if (account.accountID() == null) {
                        btnLayoutWithAccountId?.visibility = View.GONE
                        btnLayoutWithoutAccountId?.visibility = View.VISIBLE
                    } else {
                        btnLayoutWithAccountId?.visibility = View.VISIBLE
                        btnLayoutWithoutAccountId?.visibility = View.GONE
                    }

                    lastUpdatedText.text = account.lastBalanceCheck?.let { App.instance.getString(R.string.text_last_updated, Singleton.getDateFormat(it)) }
                            ?: ""
                    nanoCoins = account.balance
                }
            }

            val finalNanoCoins = nanoCoins
            mView.findViewById<TextView>(R.id.hgc_wallet_text)?.apply {
                text = Singleton.formatHGCShort(Singleton.toCoins(nanoCoins))
                setOnClickListener {
                    Dialog(context).apply {
                        requestWindowFeature(Window.FEATURE_NO_TITLE)
                        setCancelable(false)
                        setContentView(R.layout.view_custom_dailog)
                        findViewById<Button>(R.id.btn_cancel_dialog)?.apply {
                            setOnClickListener { dismiss() }
                        }
                        findViewById<TextView>(R.id.hgc_wallet_text)?.apply {
                            text = Singleton.formatHGC(Singleton.toCoins(finalNanoCoins), true)
                        }
                    }.show()
                }
            }

            mView.findViewById<TextView>(R.id.dollor_text)?.apply {
                text = Singleton.formatUSD(nanoCoins, true)
            }

            mView.findViewById<Button>(R.id.btn_pay)?.apply {
                setOnClickListener {
                    listener?.let {
                        accountsList[position].run {
                            it.onPayButtonClick(this)
                        }
                    }
                }
            }

            mView.findViewById<Button>(R.id.btn_request)?.apply {
                setOnClickListener {
                    listener?.let {
                        accountsList[position].run {
                            it.onRequestButtonClick(this)
                        }
                    }
                }
            }
            mView.findViewById<Button>(R.id.btn_request_create_account)?.apply {
                setOnClickListener {
                    listener?.let {
                        accountsList[position].run {
                            it.onRequestCreateAccountButtonClick(this)
                        }
                    }
                }
            }

            view.addView(mView, 0)
            return mView
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

        override fun saveState(): Parcelable? {
            return null
        }


        interface OverViewPagerAdapterListener {
            fun onPayButtonClick(account: Account)
            fun onRequestButtonClick(account: Account)
            fun onRequestCreateAccountButtonClick(account: Account)
        }
    }

    internal class TransactionListAdapter() : RecyclerView.Adapter<TransactionListAdapter.MyViewHolder>() {

        private var mTxnList: List<TxnRecord> = listOf()
        var transactionClick: OnTransactionClick? = null

        inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val mTextViewTime: TextView?
            private val mTextViewAccountName: TextView?
            private val mTextViewHgcWallet: TextView?
            private val mTextViewPublicKey: TextView?
            private val mTextViewDoller: TextView?
            private val mTextViewPositiveSign: TextView?
            private val mTextViewNegativeSign: TextView?

            init {
                mTextViewTime = view.findViewById<TextView>(R.id.text_time)
                mTextViewAccountName = view.findViewById<TextView>(R.id.text_account_name)
                mTextViewHgcWallet = view.findViewById<TextView>(R.id.hgc_wallet_text)
                mTextViewPublicKey = view.findViewById<TextView>(R.id.text_key)
                mTextViewDoller = view.findViewById<TextView>(R.id.dollor_text)
                mTextViewPositiveSign = view.findViewById<TextView>(R.id.text_positive_sign)
                mTextViewNegativeSign = view.findViewById<TextView>(R.id.text_negative_sign)
            }

            fun setData(txn: TxnRecord) {
                val nanoCoins = txn.amount
                when (txn.isPositive) {
                    true -> {
                        mTextViewPositiveSign?.visibility = View.VISIBLE
                        mTextViewNegativeSign?.visibility = View.GONE
                    }
                    else -> {
                        mTextViewPositiveSign?.visibility = View.GONE
                        mTextViewNegativeSign?.visibility = View.VISIBLE
                    }
                }

                txn.createdDate?.let { mTextViewTime?.text = Singleton.getDateFormat(it) }
                val accountContact = if (txn.isPositive) txn.fromAccount else txn.toAccount
                val accountId = if (txn.isPositive) txn.fromAccId else txn.toAccountId

                mTextViewAccountName?.text = if (accountContact == null || accountContact.name.isNullOrEmpty()) "UNKNOWN" else accountContact.name
                mTextViewHgcWallet?.text = Singleton.formatHGCShort(nanoCoins, true)
                mTextViewPublicKey?.text = accountId
                mTextViewDoller?.text = Singleton.formatUSD(Singleton.hgcToUSD(nanoCoins), true)
            }
        }


        fun setList(list: List<TxnRecord>) {
            mTxnList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.trasaction_list_row, parent, false)

            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val txn = mTxnList[position]
            holder.setData(txn)
            holder.itemView.setOnClickListener { transactionClick?.onTransaction(txn) }
        }

        override fun getItemCount(): Int = mTxnList.size


        interface OnTransactionClick {
            fun onTransaction(txnRecord: TxnRecord)
        }

        fun setOnTransactionClick(onTransactionClick: OnTransactionClick) {
            transactionClick = onTransactionClick
        }
    }
}