package opencrowd.hgc.hgcwallet.export_key

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.wealthfront.magellan.Navigator
import opencrowd.hgc.hgcwallet.App
import opencrowd.hgc.hgcwallet.R
import opencrowd.hgc.hgcwallet.common.Singleton
import opencrowd.hgc.hgcwallet.database.account.Account
import opencrowd.hgc.hgcwallet.ui.BaseActivity
import opencrowd.hgc.hgcwallet.ui.main.MainActivity
import opencrowd.hgc.hgcwallet.ui.main.account.AccountListScreen
import opencrowd.hgc.hgcwallet.ui.main.developertool.WebCommunicationScreen

class ExportKeyActivity : BaseActivity(), AccountListScreen.AccountPickerListener {

    private var selectedAccount: Account? = null
    private var actionBartitle: TextView? = null
    internal var homeImage: ImageView? = null
    override fun createNavigator(): Navigator {
        return Navigator.withRoot(AccountListScreen(this)).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_key)
        homeImage = findViewById(R.id.image_home)
        homeImage?.apply {
            setImageDrawable(getDrawable(R.drawable.close))
            visibility = View.VISIBLE
            setOnClickListener {
                goToMainActivity()
                finish()
            }
        }
        actionBartitle = findViewById(R.id.actionbar_title)
        actionBartitle?.text = "Export Key"
    }

    override fun onAuthSetupSuccess() {
    }

    override fun onAuthSuccess(requestCode: Int) {
    }

    override fun onAuthSetupFailed(isCancelled: Boolean) {
    }

    override fun onAuthFailed(requestCode: Int, isCancelled: Boolean) {

    }

    override fun onAccountPick(account: Account?) {
        selectedAccount = account
        IntentIntegrator(this)
                .setOrientationLocked(true)
                .setPrompt(App.instance.getString(R.string.qr_scan_instruction))
                .initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null && selectedAccount != null) {
            val pairingParams = PairingParams.fromQRCode(result.contents)
            if (pairingParams == null)
                Toast.makeText(applicationContext, R.string.invalid_qr_code, Toast.LENGTH_LONG).show()
            else {
                val deviceIP = WebServer.getWifiIP()
                if (deviceIP == null) {
                    Singleton.showToast(this, resources.getString(R.string.export_key_not_wifi_msg))
                } else
                    navigator.goTo(WebCommunicationScreen(pairingParams, selectedAccount!!, deviceIP))
            }
        } else {
            Toast.makeText(applicationContext, R.string.failed_scan_qr_code, Toast.LENGTH_LONG).show()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        goToMainActivity()
        finish()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        val currentScreen = navigator.currentScreen()
        if (currentScreen is WebCommunicationScreen)
            currentScreen.cleanUp()
        super.onDestroy()

    }
}