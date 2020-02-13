package hedera.hgc.hgcwallet.ui.onboard

import android.content.Context
import android.view.View
import android.widget.Button

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton

class WalletSetOptionScreen : Screen<WalletSetOptionView>() {

    override fun createView(context: Context): WalletSetOptionView {
        return WalletSetOptionView(context)
    }

    internal fun onSelect(restore: Boolean) {
        val nextScreen = if (restore) RestoreWalletScreen() else TermsScreen(true, Singleton.contentFromFile(Config.termsFile), "Terms & Conditions")
        navigator?.goTo(nextScreen)
    }
}

class WalletSetOptionView(context: Context) : BaseScreenView<WalletSetOptionScreen>(context) {

    init {
        View.inflate(context, R.layout.fragment_wallet_selection, this)

        findViewById<Button>(R.id.restore_wallet)?.apply {
            setOnClickListener { screen?.onSelect(true) }
        }
        findViewById<Button>(R.id.new_wallet)?.apply {
            setOnClickListener { screen?.onSelect(false) }
        }


    }


}
