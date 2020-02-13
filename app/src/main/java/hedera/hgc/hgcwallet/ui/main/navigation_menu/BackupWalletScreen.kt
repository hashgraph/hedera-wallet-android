package hedera.hgc.hgcwallet.ui.main.navigation_menu

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.local_auth.AuthManager
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class BackupWalletScreen : Screen<BackupWalletScreenView>() {

    data class Params(val wordList: List<String>)

    private val param = Params(AuthManager.getSeed()!!.toWordsList())
    override fun createView(context: Context): BackupWalletScreenView {
        return BackupWalletScreenView(context, param)
    }

    internal fun goBack() {
        navigator?.goBack()
    }
}

class BackupWalletScreenView(context: Context, val params: BackupWalletScreen.Params) : BaseScreenView<BackupWalletScreen>(context) {
    init {
        View.inflate(context, R.layout.view_backup_wallet_layout, this)
        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setTitle("BACK UP YOUR WALLET")
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener { screen?.goBack() }
        }
        findViewById<TextView>(R.id.textview_crptowords)?.setText(TextUtils.join("   ", params.wordList))

        findViewById<Button>(R.id.btn_copy)?.setOnClickListener { copyWords() }

        findViewById<ImageView>(R.id.image_copy)?.setOnClickListener { copyWords() }
    }

    private fun copyWords() {
        Singleton.copyToClipBoard(TextUtils.join(" ", params.wordList), getContext())
        Singleton.showToast(screen.activity, screen.activity.getString(R.string.copy_data_clipboard_passphrase))
    }
}