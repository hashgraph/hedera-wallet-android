package hedera.hgc.hgcwallet.ui.main.navigation_menu

import android.content.Context
import android.view.View
import android.webkit.WebView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class LicenseScreen : Screen<LicenseView>() {

    override fun createView(context: Context): LicenseView {
        return LicenseView(context)
    }
}

class LicenseView(context: Context) : BaseScreenView<LicenseScreen>(context) {
    init {
        View.inflate(context, R.layout.view_license, this)
        TitleBarWrapper(findViewById(R.id.titleBar)).setTitle("Licenses")
        findViewById<WebView>(R.id.wv_license)?.apply {
            loadUrl("file:///android_asset/open_source_licenses.html")
        }
    }
}