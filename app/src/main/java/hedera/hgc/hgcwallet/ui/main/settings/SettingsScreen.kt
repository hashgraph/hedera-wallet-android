package hedera.hgc.hgcwallet.ui.main.settings

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.local_auth.AuthListener
import hedera.hgc.hgcwallet.local_auth.AuthManager
import hedera.hgc.hgcwallet.local_auth.AuthType
import hedera.hgc.hgcwallet.ui.auth.AuthActivity
import hedera.hgc.hgcwallet.ui.auth.fingerprint.FingerprintActivityHelper
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.ui.onboard.EmptyScreen

class SettingsScreen : Screen<SettingsScreenView>(), AuthListener {

    override fun createView(context: Context): SettingsScreenView {
        return SettingsScreenView(context)
    }

    override fun onAuthSetupSuccess() {}

    override fun onAuthSuccess(requestCode: Int) {}

    override fun onAuthSetupFailed(isCancelled: Boolean) {}

    override fun onAuthFailed(requestCode: Int, isCancelled: Boolean) {
        view?.reloadUI()
    }


}

class SettingsScreenView(context: Context) : BaseScreenView<EmptyScreen>(context) {

    private val edittextName: TextView?
    private val edittextDefaultFee: TextView?
    private val pinSetup: RadioButton?
    private val fingerprintSetup: RadioButton?

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
                    getText()?.let { UserSettings.setValue(UserSettings.KEY_USER_NAME, it.toString()) }
                }

            })
        }

        edittextDefaultFee = findViewById<TextView>(R.id.edittext_default_fee)?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable) {
                    getText()?.let { UserSettings.setValue(UserSettings.KEY_DEFAULT_FEE, it.toString().toLong()) }
                }

            })
        }
        fingerprintSetup = findViewById<RadioButton>(R.id.btn_radio_fingerprint)?.apply {
            setOnCheckedChangeListener { compoundButton, b ->
                if (b && AuthManager.authType !== AuthType.FINGER) {
                    val helper = FingerprintActivityHelper()
                    helper.setup(getContext(), true, null)
                    if (helper.errorMsg == null) {
                        val authActivity = screen.activity as AuthActivity
                        authActivity.setupAuth(AuthType.FINGER)

                    } else {
                        reloadUI()
                        Singleton.showToast(screen.activity, helper.errorMsg)
                    }
                }

            }
        }
        pinSetup = findViewById<RadioButton>(R.id.btn_radio_pinsetup)?.apply {
            setOnCheckedChangeListener { compoundButton, b ->
                if (b && AuthManager.authType !== AuthType.PIN) {
                    val authActivity = screen.activity as AuthActivity
                    authActivity.setupAuth(AuthType.PIN)
                }
            }
        }
        reloadUI()


    }

    fun reloadUI() {
        fingerprintSetup?.setChecked(AuthManager.authType === AuthType.FINGER)
        pinSetup?.setChecked(AuthManager.authType === AuthType.PIN)
        val name = UserSettings.getValue(UserSettings.KEY_USER_NAME)
        if (!name.isNullOrEmpty())
            edittextName?.setText(name)

        val defaultFee = Singleton.getDefaultFee()
        edittextDefaultFee?.setText(defaultFee.toString())

    }
}