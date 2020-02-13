package hedera.hgc.hgcwallet.ui.onboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class TermsScreen(showAcceptBtn: Boolean, content: String, title: String) : Screen<TermsView>() {

    data class Params(val showAcceptBtn: Boolean, val content: String, val title: String)

    private val params = Params(showAcceptBtn, content, title)
    override fun createView(context: Context): TermsView {
        return TermsView(context, params)
    }

    internal fun onAcceptClick() {
        navigator?.goTo(NewWalletCreateScreen())
    }

    internal fun viewFullTerms() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Config.termsAndConditions))
        activity?.startActivity(browserIntent)
    }
}


class TermsView(context: Context, val params: TermsScreen.Params) : BaseScreenView<TermsScreen>(context) {

    init {
        View.inflate(context, R.layout.fragment_term_layout, this)
        TitleBarWrapper(findViewById(R.id.titleBar)).setTitle(params.title)

        findViewById<TextView>(R.id.textView)?.apply {
            text = params.content
        }

        findViewById<Button>(R.id.btn_term_accept)?.apply {
            visibility = if (params.showAcceptBtn) View.VISIBLE else View.GONE
            setOnClickListener { screen?.onAcceptClick() }
        }

        findViewById<Button>(R.id.btn_full_terms)?.apply {
            setOnClickListener { screen?.viewFullTerms() }
        }
    }

}