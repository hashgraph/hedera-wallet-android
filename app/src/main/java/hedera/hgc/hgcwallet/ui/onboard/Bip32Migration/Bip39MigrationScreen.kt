package hedera.hgc.hgcwallet.ui.onboard.Bip32Migration


import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.local_auth.AuthManager
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class Bip39MigrationScreen() : Screen<Bip39MigrationView>() {

    data class Param(val seed: HGCSeed?, val list: List<String>)

    private val param: Param

    init {
        val seed = AuthManager.getSeed()
        param = Param(seed, seed?.toWordsList() ?: listOf())
    }

    override fun createView(context: Context): Bip39MigrationView {
        return Bip39MigrationView(context, param)
    }

    internal fun onCopyButtonClick() {
        Singleton.copyToClipBoard(TextUtils.join(" ", param.list), activity)
        Singleton.showToast(activity, activity.getString(R.string.copy_data_clipboard_passphrase))
    }

    internal fun onDoneButtonClick() {
        UserSettings.setValue(UserSettings.KEY_HAS_SHOWN_BIP39_MNEMONIC, true)
        navigator?.goBack()
    }

}

class Bip39MigrationView(context: Context, val param: Bip39MigrationScreen.Param) : BaseScreenView<Bip39MigrationScreen>(context) {

    init {
        View.inflate(context, R.layout.view_bip39_migration, this)
        val titleBar = TitleBarWrapper(findViewById(R.id.titleBar))
        titleBar.setTitle(resources.getString(R.string.backup_your_wallet))

        findViewById<TextView>(R.id.textview_crptowords)?.apply {
            text = TextUtils.join("   ", param.list)
        }

        findViewById<Button>(R.id.btn_copy)?.apply {
            setOnClickListener {
                screen?.onCopyButtonClick()
            }
        }

        findViewById<Button>(R.id.btn_done)?.apply {
            setOnClickListener {
                screen.onDoneButtonClick()
            }
        }


    }
}