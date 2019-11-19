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

package hedera.hgc.hgcwallet.ui.main.pay

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.google.zxing.integration.android.IntentIntegrator
import com.squareup.okhttp.MediaType
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.*
import hedera.hgc.hgcwallet.app_intent.TransferRequestParams
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.TaskExecutor
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.database.contact.Contact
import hedera.hgc.hgcwallet.database.request.PayRequest
import hedera.hgc.hgcwallet.hapi.TransferParam
import hedera.hgc.hgcwallet.hapi.tasks.TransferTaskAPI
import hedera.hgc.hgcwallet.modals.ExchangeInfo
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.ui.BaseActivity
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.ui.main.MainActivity
import hedera.hgc.hgcwallet.ui.main.account.AccountListScreen
import hedera.hgc.hgcwallet.ui.main.pay.contacts.ContactListScreen
import hedera.hgc.hgcwallet.ui.scan.QRScanListener
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class PayScreen(fromAccount: Account, type: PayType = PayType.Pay) : Screen<PayScreenView>(), AccountListScreen.AccountPickerListener, ContactListScreen.ContactPickerListener, QRScanListener {

    data class Params(var fromAccount: Account = Account(), val type: PayType = PayType.Pay, var showToAccount: Boolean = false, var addNote: Boolean = false, var toAccIdStr: String = "", var toName: String = "", var amountStr: String = "", var notes: String = "", var fee: Long = Singleton.getDefaultFee(), var host: String = "")

    private val param: Params
    private val disposables = CompositeDisposable()

    init {
        param = Params(fromAccount, type)
    }

    constructor(fromAccount: Account, toAccount: PayRequest, type: PayType) : this(fromAccount, type) {
        param.apply {
            showToAccount = true
            toAccIdStr = toAccount.accountId
            toName = toAccount.name ?: ""
            amountStr = Singleton.format(Singleton.toCoins(toAccount.amount))
        }

    }

    override fun onShow(context: Context?) {
        super.onShow(context)
        updateFee()
        view?.reloadData()
    }

    override fun createView(context: Context): PayScreenView {
        return PayScreenView(context, param)
    }

    override fun onAccountPick(account: Account) {
        param.fromAccount = account
        navigator.goBackTo(this)
    }

    override fun onContactPick(contact: Contact) {
        param.apply {
            showToAccount = true
            toAccIdStr = contact.accountId
            toName = contact.name ?: ""
            param.host = contact.getHost() ?: ""
        }
        navigator.goBackTo(this)
    }

    override fun onQRScanFinished(success: Boolean, result: String?) {
        if (!success) return
        if (result != null) {

            when (param.type) {
                PayType.Pay -> TransferRequestParams.fromBarCode(result)?.let { requestParams ->

                    param.apply {
                        toAccIdStr = requestParams.account.stringRepresentation()
                        toName = requestParams.name ?: ""
                        amountStr = Singleton.toCoins(requestParams.amount).toString() + ""
                        requestParams.note?.let {
                            notes = it
                            addNote = true
                        }
                        showToAccount = true
                    }
                    view.reloadData()
                }
                PayType.PayToExchange -> ExchangeInfo.fromQRCode(result)?.let { info ->
                    param.apply {
                        toAccIdStr = info.accountId.stringRepresentation()
                        toName = info.name
                        host = info.host
                        notes = info.memo ?: ""
                        showToAccount = true
                    }
                    view.reloadData()
                }
            }

        }
    }


    fun updateFee() {
        if (param.fromAccount.accountID() == null) return
        param.fee = Singleton.getDefaultFee()

        val view = view
        view?.updateFee(param.fee)
    }


    internal fun onPayButtonClick() {
        var error: String? = null
        val toAccountID = HGCAccountID.fromString(param.toAccIdStr)
        var amount = 0.0

        try {
            amount = java.lang.Double.parseDouble(param.amountStr)
        } catch (e: Exception) {
        }


        if (toAccountID == null) {
            error = "Invalid to account id"
        } else if (amount == 0.0) {
            error = "Invalid amount"
        }

        if (getByteCount(param.notes) > Config.maxAllowedMemoLength)
            error = "Note is too long"


        val activity = activity as BaseActivity
        if (error == null) {
            activity.showActivityProgress("Please wait")
            val transferParam = TransferParam(toAccountID!!, Singleton.toNanoCoins(amount), param.notes, param.fee, false, param.toName)
            val task = TransferTaskAPI(transferParam, param.fromAccount)
            val taskExecutor = TaskExecutor()
            taskExecutor.setListner {
                activity.hideActivityProgress()
                if (task.error == null) {
                    if (activity is MainActivity) {
                        activity.synchronizeData(true, false)
                    }
                    Singleton.showToast(activity, activity.getString(R.string.transaction_submitted_successfully))
                    navigator.goBack()

                } else {
                    task.error?.let { Singleton.showToast(activity, it) }
                }
            }
            taskExecutor.execute(task)
        } else {
            Singleton.showToast(getActivity(), error)
        }
    }

    internal fun onPayExchangeButtonClick() {
        var amount = 0.0
        try {
            amount = java.lang.Double.parseDouble(param.amountStr)
        } catch (e: Exception) {
        }

        val toAccountID = HGCAccountID.fromString(param.toAccIdStr)
        val MEDIA_TYPE_MARKDOWN = MediaType.parse("application/octet-stream")
        val hostUrl = ExchangeInfo.toHttpUrl(param.host)
        val activity = activity as BaseActivity

        var error = ""
        if (param.fromAccount.accountID() == null) {
            error = "Account is not linked"
        }

        if (toAccountID == null) {
            error = "Invalid to account id"
        } else if (amount == 0.0) {
            error = "Invalid amount"
        }

        if (getByteCount(param.notes) > Config.maxAllowedMemoLength)
            error = "Note is too long"

        if (error.isEmpty()) {
            activity.showActivityProgress("Please wait")
            val transferParam = TransferParam(toAccountID!!, Singleton.toNanoCoins(amount), param.notes, param.fee, forThirdParty(), param.toName)
            val txn = transferParam.getTransaction(param.fromAccount.getTransactionBuilder())
            disposables.add(
                    (+Single.fromCallable {
                        DBHelper.createContact(toAccountID, param.toName, param.host, true)
                    }).flatMap {
                        +postURL(hostUrl, txn.toByteArray())
                    }.flatMap {
                        Single.fromCallable {
                            DBHelper.createTransaction(txn, param.fromAccount.accountID())
                        }
                    }.subscribe({
                        activity.hideActivityProgress()
                        val success = true
                        if (success) {
                            if (activity is MainActivity) {
                                activity.synchronizeData(true, false)
                            }
                            Singleton.showToast(activity, activity.getString(R.string.transaction_submitted_successfully))
                            navigator.goBack()
                        } else
                            Singleton.showToast(getActivity(), error)

                    }, {
                        activity.hideActivityProgress()
                        Singleton.showToast(getActivity(), error)
                    })

            )
        } else
            Singleton.showToast(getActivity(), error)

    }

    fun forThirdParty() = when (param.type) {
        PayType.PayToExchange -> true
        PayType.Pay -> false
    }

    internal fun onChangeTextClick() {
        navigator?.goTo(AccountListScreen(this))
    }

    internal fun onCancelClick() {
        param.showToAccount = false
        view?.setToAccountViewVisible(false)
    }

    internal fun onExistingClick() {
        navigator?.goTo(ContactListScreen(this, forThirdParty()))
    }

    internal fun onNewContactClick() {
        param.showToAccount = true
        view?.setToAccountViewVisible(true)
    }

    internal fun onScanClick() {
        IntentIntegrator(activity)
                .setOrientationLocked(true)
                .setPrompt("Place a QRcode inside the rectangle")
                .initiateScan()
    }

    internal fun onAddNoteClick(isChecked: Boolean) {
        param.addNote = isChecked
        view?.setNoteTextFieldVisible(isChecked)
    }

    internal fun goBack() {
        navigator?.goBack()
    }

}

enum class PayType {
    PayToExchange,
    Pay
}

class PayScreenView(context: Context, val param: PayScreen.Params) : BaseScreenView<PayScreen>(context) {

    private val accountName: TextView?
    private val publicKey: TextView?
    private val changeText: TextView?
    private val cancel: TextView?
    private val feeTextView: TextView?
    private val feeTitleTextView: TextView?
    private val noteText: TextView?
    private val noteLayout: LinearLayout?
    private val newButton: Button?
    private val scanButton: Button?
    private val payButton: Button?
    private val existingButton: Button?
    private val addNote: CheckBox?
    private val noteEditText: EditText?
    private val hgcAmountEditText: EditText?
    private val editTextDollarAmount: EditText?
    private val accountIdEditText: EditText?
    private val hostEditText: EditText?
    private val accountNameEditText: EditText?
    private val payButtonsLayout: LinearLayout?
    private val newButtonLayout: LinearLayout?
    private val amountRelativeLayout: RelativeLayout?
    private val dollarAmountRelativeLayout: RelativeLayout?
    private val hgcAmountRelativeLayout: RelativeLayout?
    private val titleBarWrapper: TitleBarWrapper
    private var dollarAmountTextWatcher: TextWatcher? = null
    private var hgcAmountTextWatcher: TextWatcher? = null

    constructor(context: Context) : this(context, PayScreen.Params())

    init {
        View.inflate(context, R.layout.view_pay_layout, this)
        accountName = findViewById<TextView>(R.id.text_account_name)
        publicKey = findViewById(R.id.text_public_key)
        changeText = findViewById<TextView>(R.id.text_change)?.apply {
            setOnClickListener { screen?.onChangeTextClick() }
        }
        cancel = findViewById<TextView>(R.id.text_cancel)?.apply {
            setOnClickListener { screen?.onCancelClick() }
        }
        newButton = findViewById<Button>(R.id.btn_new)?.apply {
            setOnClickListener { screen?.onNewContactClick() }
        }
        scanButton = findViewById<Button>(R.id.btn_scan)?.apply {
            setOnClickListener { screen?.onScanClick() }
        }
        payButton = findViewById<Button>(R.id.btn_pay)?.apply {
            setOnClickListener {
                popluateData()
                when (param.type) {
                    PayType.Pay -> screen?.onPayButtonClick()
                    PayType.PayToExchange -> screen?.onPayExchangeButtonClick()
                }

            }
        }
        existingButton = findViewById<Button>(R.id.btn_existing)?.apply {
            setOnClickListener { screen?.onExistingClick() }
        }
        addNote = findViewById<CheckBox>(R.id.add_note)?.apply {
            setOnCheckedChangeListener { compoundButton, isChecked -> screen?.onAddNoteClick(isChecked) }
        }

        feeTitleTextView = findViewById(R.id.text_fee)
        feeTextView = findViewById(R.id.text_fee_value)

        noteLayout = findViewById(R.id.layout_note)
        noteText = findViewById(R.id.text_note)
        noteEditText = findViewById<EditText>(R.id.edittext_note)

        accountIdEditText = findViewById<EditText>(R.id.edittext_account_id)
        hostEditText = findViewById<EditText>(R.id.edittext_host)
        accountNameEditText = findViewById<EditText>(R.id.edittext_account_name)
        hgcAmountEditText = findViewById<EditText>(R.id.edittext_hgc_amount)
        editTextDollarAmount = findViewById<EditText>(R.id.edittext_dollor_amount)

        dollarAmountTextWatcher = object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                try {
                    val dollar = java.lang.Double.parseDouble(editable.toString())
                    val coins = Singleton.toCoins(Singleton.USDtoHGC(dollar))
                    hgcAmountTextWatcher?.let { setText(hgcAmountEditText, it, Singleton.toString(coins)) }
                } catch (e: Exception) {
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }

        hgcAmountTextWatcher = object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                try {
                    val coins = java.lang.Double.parseDouble(editable.toString())
                    val dollar = Singleton.hgcToUSD(Singleton.toNanoCoins(coins))
                    dollarAmountTextWatcher?.let { setText(editTextDollarAmount, it, Singleton.toString(dollar)) }
                } catch (e: Exception) {
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }

        editTextDollarAmount?.addTextChangedListener(dollarAmountTextWatcher)
        hgcAmountEditText?.addTextChangedListener(hgcAmountTextWatcher)


        payButtonsLayout = findViewById(R.id.pay_type_btn_layout)
        newButtonLayout = findViewById(R.id.new_btn_open_layout)
        amountRelativeLayout = findViewById(R.id.amount_relative_layout)
        dollarAmountRelativeLayout = findViewById(R.id.dollar_amount_layout)
        hgcAmountRelativeLayout = findViewById(R.id.hgc_amount_layout)


        titleBarWrapper = TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener { screen?.goBack() }
        }


        when (param.type) {
            PayType.PayToExchange -> {
                dollarAmountRelativeLayout?.visibility = View.GONE
                (hgcAmountRelativeLayout?.getLayoutParams() as? (LinearLayout.LayoutParams))?.weight = 2f
                titleBarWrapper.setTitle("Pay to Third Party")
                addNote?.visibility = View.GONE
                feeTitleTextView?.visibility = View.GONE
                feeTextView?.visibility = View.GONE
                setNoteTextFieldVisible(false)
            }
            PayType.Pay -> {
                titleBarWrapper.setTitle("PAY")
            }
        }

        reloadData()
    }

    private fun popluateData() {
        accountIdEditText?.run {
            param.toAccIdStr = text.toString()
        }
        hostEditText?.run {
            param.host = text.toString()
        }

        accountNameEditText?.run {
            param.toName = text.toString()
        }
        hgcAmountEditText?.run {
            param.amountStr = text.toString()
        }
        noteEditText?.run {
            param.notes = text.toString()
        }
    }

    private fun setText(editText: EditText?, textWatcher: TextWatcher, text: String) {
        editText?.apply {
            removeTextChangedListener(textWatcher)
            setText(text)
            addTextChangedListener(textWatcher)
        }
    }

    fun reloadData() {
        accountName?.setText(param.fromAccount.name)

        publicKey?.text = if (param.fromAccount.accountID() != null) {
            param.fromAccount.accountID()!!.stringRepresentation()
        } else {
            val shortKey = Singleton.publicKeyStringShort(param.fromAccount)
            context.getString(R.string.text_key_short, shortKey)
        }

        hgcAmountTextWatcher?.let { setText(hgcAmountEditText, it, param.amountStr) }

        try {
            val coins = java.lang.Double.parseDouble(param.amountStr)
            val dollar = Singleton.hgcToUSD(Singleton.toNanoCoins(coins))
            dollarAmountTextWatcher?.let { setText(editTextDollarAmount, it, Singleton.toString(dollar)) }

        } catch (e: Exception) {

        }

        noteEditText?.setText(param.notes)
        accountIdEditText?.setText(param.toAccIdStr)
        accountNameEditText?.setText(param.toName)
        hostEditText?.setText(param.host)
        addNote?.isChecked = param.addNote
        updateFee(param.fee)
        setToAccountViewVisible(param.showToAccount)
        setNoteTextFieldVisible(param.addNote)
    }

    fun updateFee(fee: Long) {
        feeTextView?.setText(Singleton.formatHGC(fee, true))
    }


    fun setToAccountViewVisible(visible: Boolean) {
        if (visible) {
            payButtonsLayout?.setVisibility(View.INVISIBLE)
            newButtonLayout?.setVisibility(View.VISIBLE)
            hostEditText?.visibility = when (param.type) {
                PayType.PayToExchange -> View.VISIBLE
                else -> View.GONE
            }
            val p = RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            p.addRule(RelativeLayout.BELOW, R.id.new_btn_open_layout)
            p.setMargins(0, 100, 0, 0)
            amountRelativeLayout?.setLayoutParams(p)

        } else {
            payButtonsLayout?.setVisibility(View.VISIBLE)
            newButtonLayout?.setVisibility(View.INVISIBLE)
            val p = RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            p.addRule(RelativeLayout.BELOW, R.id.pay_type_btn_layout)
            p.setMargins(0, 30, 0, 0)
            amountRelativeLayout?.setLayoutParams(p)
        }
    }

    fun setNoteTextFieldVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        noteLayout?.visibility = visibility
    }


}