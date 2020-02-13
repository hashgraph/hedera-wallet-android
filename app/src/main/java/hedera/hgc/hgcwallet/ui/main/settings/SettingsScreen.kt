package hedera.hgc.hgcwallet.ui.main.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.database.wallet.Wallet
import hedera.hgc.hgcwallet.local_auth.AuthListener
import hedera.hgc.hgcwallet.local_auth.AuthManager
import hedera.hgc.hgcwallet.local_auth.AuthType
import hedera.hgc.hgcwallet.ui.LauncherActivity
import hedera.hgc.hgcwallet.ui.auth.AuthActivity
import hedera.hgc.hgcwallet.ui.auth.fingerprint.FingerprintActivityHelper
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.ui.onboard.EmptyScreen
import java.lang.Exception

class SettingsScreen : Screen<SettingsScreenView>(), AuthListener {


    data class Params(val name: String?, val defaultFee: String, var authType: AuthType)

    private val params = Params(UserSettings.getValue(UserSettings.KEY_USER_NAME), Singleton.format(Singleton.toCoins(Singleton.getDefaultFee())), AuthManager.authType)

    override fun createView(context: Context): SettingsScreenView {
        return SettingsScreenView(context, params)
    }

    override fun onAuthSetupSuccess() {
        params.authType = AuthManager.authType
        view?.reloadUI()
    }

    override fun onAuthSuccess(requestCode: Int) {

    }

    override fun onAuthSetupFailed(isCancelled: Boolean) {}

    override fun onAuthFailed(requestCode: Int, isCancelled: Boolean) {
        view?.reloadUI()
    }

    internal fun onBiometricClick() {
        if (AuthManager.authType != AuthType.FINGER) {
            val helper = FingerprintActivityHelper()
            helper.setup(activity, true, null)
            if (helper.errorMsg == null)
                (activity as? AuthActivity)?.setupAuth(AuthType.FINGER)
            else
                Singleton.showToast(activity, helper.errorMsg)

        }
    }

    internal fun onPinClick() {
        (activity as? AuthActivity)?.setupAuth(AuthType.PIN)
    }

    override fun onHide(context: Context?) {
        super.onHide(context)
        view?.getDefaultFee()?.let {
            onFeeChange(it)
        }
    }

    internal fun onFeeChange(text: CharSequence?) {
        val result = text?.toString()?.toDoubleOrNull()
        if (result == null)
            activity?.run { Singleton.showDefaultAlert(this, getString(R.string.invalid_fee_title), getString(R.string.invalid_fee_message)) }
        else {

            UserSettings.setValue(UserSettings.KEY_DEFAULT_FEE, Singleton.toNanoCoins(result))
        }
    }
}


class SettingsScreenView(context: Context, val params: SettingsScreen.Params) : BaseScreenView<SettingsScreen>(context) {

    private val edittextName: TextView?
    private val edittextDefaultFee: EditText?
    private val btnPin: Button?
    private val btnFingerprint: Button?

    init {
        View.inflate(context, R.layout.view_settings_layout, this)

        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setTitle(R.string.text_profile)
        }

        edittextName = findViewById<TextView>(R.id.edittext_name)?.apply {
            hint = "Name account locally"
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable) {
                    text?.toString()?.let { UserSettings.setValue(UserSettings.KEY_USER_NAME, it) }
                }

            })
        }

        edittextDefaultFee = findViewById<EditText>(R.id.edittext_default_fee)


        btnFingerprint = findViewById<Button>(R.id.btn_fingerprint)?.apply {
            setOnClickListener { screen?.onBiometricClick() }
        }

        btnPin = findViewById<Button>(R.id.btn_pin)?.apply {
            setOnClickListener { screen?.onPinClick() }
        }


        reloadUI()


    }


    fun reloadUI() {

        if (!params.name.isNullOrEmpty())
            edittextName?.text = params.name

        edittextDefaultFee?.setText(params.defaultFee)

        btnPin?.visibility = View.VISIBLE
        btnFingerprint?.visibility = View.VISIBLE

        when (params.authType) {
            AuthType.PIN -> {
                btnPin?.setText(R.string.change_pin)
                btnFingerprint?.setText(R.string.enable_biometric)
            }
            AuthType.FINGER -> {
                btnPin?.setText(R.string.enable_pin)
                btnFingerprint?.visibility = View.GONE
            }
            AuthType.UNKNOWN -> {
                btnPin?.setText(R.string.enable_pin)
                btnFingerprint?.setText(R.string.enable_biometric)
            }
        }
    }

    fun getDefaultFee() = edittextDefaultFee?.text.toString()

}