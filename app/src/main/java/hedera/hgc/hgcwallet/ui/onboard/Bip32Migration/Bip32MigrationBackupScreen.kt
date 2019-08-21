package hedera.hgc.hgcwallet.ui.onboard.Bip32Migration

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class Bip32MigrationBackupScreen(handler: IBip32Migration) : Screen<Bip32MigrationBackupView>() {

    data class Param(val seed: HGCSeed, val list: List<String>, val handler: IBip32Migration)

    private val param: Param

    init {
        val seed = Singleton.createSeed()
        param = Param(seed, seed.toWordsList(), handler)
    }

    override fun createView(context: Context): Bip32MigrationBackupView {
        return Bip32MigrationBackupView(context, param)
    }

    internal fun onCopyButtonClick() {
        Singleton.copyToClipBoard(TextUtils.join(" ", param.list), activity)
        Singleton.showToast(activity, getActivity().getString(R.string.copy_data_clipboard_passphrase))
    }

    internal fun onConfirmButtonClick(accountID: String) {
        val acctId = HGCAccountID.fromString(accountID)
        if (acctId != null)
            if (acctId == param.handler.getAccountID())
                navigator?.goTo(Bip32MigrationConfirmScreen(param.seed, param.handler))
            else
                Singleton.showToast(activity, "Account ID did not match")
        else
            Singleton.showToast(activity, "Please enter a valid Account ID")
    }

    internal fun onCloseButtonClick() {
        navigator?.goBack()
    }

    override fun handleBack(): Boolean {
        return true
    }

}

class Bip32MigrationBackupView(context: Context, val param: Bip32MigrationBackupScreen.Param) : BaseScreenView<Bip32MigrationBackupScreen>(context) {

    private val editTextFullAccountId: EditText?


    init {
        View.inflate(context, R.layout.view_bip32_migration_backup, this)

        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setTitle("")
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener {
                screen?.onCloseButtonClick()
            }
        }

        editTextFullAccountId = findViewById<EditText>(R.id.edittext_full_account_id)

        findViewById<TextView>(R.id.textview_crptowords)?.apply {
            text = TextUtils.join("   ", param.list) + "\n\n Account ID: ${param.handler.getAccountID().stringRepresentation()} "
        }

        findViewById<Button>(R.id.btn_copy)?.apply {
            setOnClickListener {
                screen?.onCopyButtonClick()
            }
        }

        findViewById<Button>(R.id.btn_confirm)?.apply {
            setOnClickListener {
                editTextFullAccountId?.text?.let {
                    screen?.onConfirmButtonClick(it.toString())
                }

            }
        }

    }
}