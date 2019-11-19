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

package hedera.hgc.hgcwallet.ui.main.transcation

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.*
import com.google.zxing.integration.android.IntentIntegrator
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.app_intent.CreateAccountRequestParams
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.TaskExecutor
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.hapi.CreateAccountParams
import hedera.hgc.hgcwallet.hapi.tasks.CreateAccountTask
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.PublicKeyAddress
import hedera.hgc.hgcwallet.ui.BaseActivity
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.ui.main.account.AccountListScreen
import hedera.hgc.hgcwallet.ui.scan.QRScanListener
import android.text.InputFilter
import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.getByteCount


data class CreateAccountScreenModel(var fromAccount: Account) {
    var accountName: String = ""
    var publicKeyString: String = ""
    var fee: Long = Singleton.getDefaultFee()
    var amountHBar: String = "1.0"
    var isNewSelected: Boolean = false
    var notes: String = ""

    fun publicKey() = PublicKeyAddress.from(publicKeyString)


    var amount: Long?
        set(value) {
            amountHBar = value?.let { Singleton.formatHGCShort(Singleton.toCoins(it)) } ?: ""
        }
        get() {
            return amountHBar.toDoubleOrNull()?.let {
                Singleton.toNanoCoins(it)
            }
        }

    @Throws(Exception::class)
    fun parseQR(code: String) {
        val obj = CreateAccountRequestParams.from(code)
        if (obj != null) {
            accountName = ""
            publicKeyString = obj.publicKeyAddress.stringRepresentation()
            obj.initialAmount?.let { if (it > 0) amount = it }

        } else
            throw Exception("Invalid QR Code")
    }

    fun validateParams(): String? {
        return when {
            fromAccount.accountID() == null -> "From account is not linked"
            publicKey() == null -> "Invalid public key"
            amount == null || amount!! <= 0 -> "Invalid amount"
            getByteCount(notes) > Config.maxAllowedMemoLength -> "Note is too long"
            else -> null
        }
    }
}


class CreateAccountScreen(fromAccount: Account) : Screen<CreateAccountView>(), QRScanListener, AccountListScreen.AccountPickerListener {


    private val params: CreateAccountScreenModel = CreateAccountScreenModel(fromAccount)

    override fun createView(context: Context): CreateAccountView {
        return CreateAccountView(context, params)
    }

    internal fun onScanClick() {
        IntentIntegrator(activity)
                .setOrientationLocked(true)
                .setPrompt("Place a QRcode inside the rectangle")
                .initiateScan()
    }

    internal fun goBack() {
        navigator?.goBack()
    }

    internal fun onChangeTextClick() {
        navigator?.goTo(AccountListScreen(this))
    }

    internal fun onPayButtonClick() {
        params.validateParams()?.let {
            Singleton.showToast(getActivity(), it)
            return
        }


        val baseActivity = (activity as? BaseActivity)
        baseActivity?.showActivityProgress("Please wait")
        val createAccountParams = CreateAccountParams(params.publicKey()!!, params.amount!!, params.fee, params.notes)
        val task = CreateAccountTask(createAccountParams, params.fromAccount)
        val taskExecutor = TaskExecutor()
        taskExecutor.setListner {
            baseActivity?.hideActivityProgress()
            if (task.error == null) {
                task.accountId?.let { onTransactionSuccess(it) }
            } else {
                Singleton.showToast(activity, task.error)
            }
        }
        taskExecutor.execute(task)
    }

    private fun onTransactionSuccess(newAccountId: HGCAccountID) {
        activity?.let {
            Singleton.showDefaultAlert(it, "Account created successfully", " Your new account ID is: ${newAccountId.stringRepresentation()}", DialogInterface.OnClickListener { dialogInterface, i ->
                goBack()
            })
        }
    }

    override fun onQRScanFinished(success: Boolean, result: String?) {
        try {
            result?.let { params.parseQR(it) }
            view?.reloadData()
        } catch (e: Exception) {
            Singleton.showToast(getActivity(), e.message ?: "Invalid QR Code")
        }

    }

    override fun onAccountPick(account: Account) {
        params.fromAccount = account
        navigator.goBackTo(this)
    }
}

class CreateAccountView(context: Context, val params: CreateAccountScreenModel) : BaseScreenView<CreateAccountScreen>(context) {
    private val titleBarWrapper: TitleBarWrapper
    private val accountName: TextView?
    private val toPublicKey: EditText?
    private val fromPublicKey: TextView?
    private val changeText: TextView?
    private val scanText: TextView?
    private val payButton: Button?
    private val feeTextView: TextView?
    private val initialAmountTextView: TextView?
    private val noteLayout: LinearLayout?
    private val noteEditText: EditText?

    init {
        View.inflate(context, R.layout.view_create_account, this)
        titleBarWrapper = TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener { screen?.goBack() }
            setTitle("Create Account")
        }

        noteLayout = findViewById(R.id.layout_note)
        noteEditText = findViewById<EditText>(R.id.edittext_note)

        accountName = findViewById<TextView>(R.id.text_account_name)
        fromPublicKey = findViewById<TextView>(R.id.text_public_key)

        toPublicKey = findViewById(R.id.edittext_public_key)

        changeText = findViewById<TextView>(R.id.text_change)?.apply {
            setOnClickListener { screen?.onChangeTextClick() }
        }

        scanText = findViewById<TextView>(R.id.text_scan)?.apply {
            setOnClickListener { screen?.onScanClick() }
        }


        payButton = findViewById<Button>(R.id.btn_pay)?.apply {
            setOnClickListener {
                populateData()
                screen?.onPayButtonClick()
            }
        }

        feeTextView = findViewById(R.id.text_fee_value)
        initialAmountTextView = findViewById(R.id.text_amount_value)

        findViewById<RelativeLayout>(R.id.dollar_amount_layout)?.apply {
            visibility = View.GONE
        }
        findViewById<RelativeLayout>(R.id.hgc_amount_layout)?.apply {
            (layoutParams as? (LinearLayout.LayoutParams))?.weight = 2f
        }

        reloadData()
    }

    private fun populateData() {
        toPublicKey?.let { params.publicKeyString = it.text.toString() }
        noteEditText?.let { params.notes = it.text.toString() }
    }


    fun reloadData() {
        accountName?.setText(params.fromAccount.name)

        fromPublicKey?.text = if (params.fromAccount.accountID() != null) {
            params.fromAccount.accountID()!!.stringRepresentation()
        } else {
            val shortKey = Singleton.publicKeyStringShort(params.fromAccount)
            context.getString(R.string.text_key_short, shortKey)
        }
        toPublicKey?.setText(params.publicKeyString)

        params.amount?.let { initialAmountTextView?.text = Singleton.formatHGC(it, true) }
        noteEditText?.setText(params.notes)

        updateFee(params.fee)
    }

    fun updateFee(fee: Long) {
        feeTextView?.text = Singleton.formatHGC(fee, true)
    }
}

