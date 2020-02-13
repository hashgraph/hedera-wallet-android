package hedera.hgc.hgcwallet.ui.onboard

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.local_auth.AuthManager
import hedera.hgc.hgcwallet.local_auth.AuthType
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.KeyDerivation
import hedera.hgc.hgcwallet.ui.auth.AuthActivity
import hedera.hgc.hgcwallet.ui.auth.fingerprint.FingerprintActivityHelper
import hedera.hgc.hgcwallet.ui.main.MainActivity

class PinSetUpOptionScreen(seed: HGCSeed, keyDerivation: KeyDerivation, accountId: HGCAccountID?) : Screen<PinSetUpOptionView>() {

    data class Param(val keyDerivation: KeyDerivation, val seed: HGCSeed, val accountId: HGCAccountID?)

    private val param: Param

    init {
        param = Param(keyDerivation, seed, accountId)
    }

    override fun createView(context: Context): PinSetUpOptionView {
        return PinSetUpOptionView(context, param)
    }

    fun setupWallet() {
        AuthManager.saveSeed(param.seed.getEntropy())
        Singleton.setupWallet(param.keyDerivation)
        param.accountId?.let {
            val defaultAccount = DBHelper.getAllAccounts()[0]
            defaultAccount.setAccountID(it)
            DBHelper.saveAccount(defaultAccount)
        }

        activity?.let {
            val intent = Intent(it, MainActivity::class.java)
            it.startActivity(intent)
            it.finish()
        }

    }

    internal fun onSetupButtonClick() {
        (activity as? AuthActivity)?.run {
            setupAuth(AuthType.PIN)
        }
    }

    internal fun onEnableFingerPrintClick() {
        FingerprintActivityHelper().run {
            setup(activity, true, null)
            if (errorMsg == null)
                (activity as? AuthActivity)?.run {
                    setupAuth(AuthType.FINGER)
                }
            else
                Singleton.showToast(activity, errorMsg)

        }
    }
}

class PinSetUpOptionView(context: Context, param: PinSetUpOptionScreen.Param) : BaseScreenView<PinSetUpOptionScreen>(context) {
    init {
        View.inflate(context, R.layout.view_pinsetup_layout, this)


        findViewById<Button>(R.id.btn_setup_pin)?.apply {
            text = context.getString(R.string.btn_text_setup_pin, Config.passcodeLength.toString())
            setOnClickListener {
                screen?.onSetupButtonClick()
            }

        }

        findViewById<Button>(R.id.btn_enable_fingerprint)?.apply {
            setOnClickListener {
                screen?.onEnableFingerPrintClick()
            }
        }

    }
}