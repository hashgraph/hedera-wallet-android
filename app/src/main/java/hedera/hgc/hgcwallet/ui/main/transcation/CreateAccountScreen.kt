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
import hedera.hgc.hgcwallet.hapi.tasks.CreateAccountTask
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.PublicKeyAddress
import hedera.hgc.hgcwallet.ui.BaseActivity
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.ui.main.account.AccountListScreen
import hedera.hgc.hgcwallet.ui.scan.QRScanListener

data class CreateAccountScreenModel(var fromAccount: Account) {
    var accountName: String = ""
    var publicKeyString: String = ""
    var fee: Long = Singleton.getDefaultFee()
    var amountHBar: String = ""
    var isNewSelected: Boolean = false

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
            obj.initialAmount?.let { amount = it }

        } else
            throw Exception("Invalid QR Code")
    }

    fun validateParams(): String? {
        return when {
            fromAccount.accountID() == null -> "From account is not linked"
            publicKey() == null -> "Invalid public key"
            amount == null || amount!! <= 0 -> "Invalid amount"
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
        val task = CreateAccountTask(params.fromAccount, params.publicKey()!!, params.amount!!, params.fee)
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

    override fun onAccountPick(account: Account?) {
        account?.let {
            params.fromAccount = it
            navigator.goBackTo(this)
        }

    }
}

class CreateAccountView(context: Context, val params: CreateAccountScreenModel) : BaseScreenView<CreateAccountScreen>(context) {
    private val titleBarWrapper: TitleBarWrapper
    private val accountName: TextView?
    private val toPublicKey: EditText?
    private val fromPublicKey: TextView?
    private val changeText: TextView?
    private val scanText: TextView?
    private val hgcAmountEditText: EditText?
    private val payButton: Button?
    private val feeTextView: TextView?

    init {
        View.inflate(context, R.layout.view_create_account, this)
        titleBarWrapper = TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener { screen?.goBack() }
            setTitle("Create Account")
        }

        accountName = findViewById<TextView>(R.id.text_account_name)
        fromPublicKey = findViewById<TextView>(R.id.text_public_key)

        toPublicKey = findViewById(R.id.edittext_public_key)

        changeText = findViewById<TextView>(R.id.text_change)?.apply {
            setOnClickListener { screen?.onChangeTextClick() }
        }

        scanText = findViewById<TextView>(R.id.text_scan)?.apply {
            setOnClickListener { screen?.onScanClick() }
        }

        hgcAmountEditText = findViewById<EditText>(R.id.edittext_hgc_amount)


        payButton = findViewById<Button>(R.id.btn_pay)?.apply {
            setOnClickListener {
                params.publicKeyString = toPublicKey.text.toString()
                params.amountHBar = hgcAmountEditText.text.toString()
                screen?.onPayButtonClick()
            }
        }

        feeTextView = findViewById(R.id.text_fee_value)

        findViewById<RelativeLayout>(R.id.dollar_amount_layout)?.apply {
            visibility = View.GONE
        }
        findViewById<RelativeLayout>(R.id.hgc_amount_layout)?.apply {
            (layoutParams as? (LinearLayout.LayoutParams))?.weight = 2f
        }

        reloadData()
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

        params.amount?.let { hgcAmountEditText?.setText(it.toString()) }

        updateFee(params.fee)
    }

    fun updateFee(fee: Long) {
        feeTextView?.setText(Singleton.formatHGC(fee, true))
    }
}

