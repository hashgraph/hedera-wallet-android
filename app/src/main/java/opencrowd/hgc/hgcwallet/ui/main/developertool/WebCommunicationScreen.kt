package opencrowd.hgc.hgcwallet.ui.main.developertool

import android.content.Context
import android.os.Handler
import android.view.View
import android.widget.TextView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import opencrowd.hgc.hgcwallet.R
import opencrowd.hgc.hgcwallet.export_key.WebServer
import opencrowd.hgc.hgcwallet.export_key.WebServerListener
import opencrowd.hgc.hgcwallet.export_key.PairingParams
import opencrowd.hgc.hgcwallet.database.account.Account

class WebCommunicationScreen(val pairingParams: PairingParams, val account: Account, val ip: String) : Screen<WebCommunicationScreenView>() {
    var webServer: WebServer? = null
    val pinCode = pairingParams.getPIN(ip)

    init {
        val handler = Handler()
        webServer = WebServer(8080, object : WebServerListener {
            override fun getPairingParams(): PairingParams = pairingParams
            override fun getAccount(): Account = account
            override fun requestHandled(success: Boolean) {
                handler.postDelayed({
                    cleanUp()
                    if (navigator.atRoot())
                        navigator.currentScreen().getActivity().onBackPressed()
                    else
                        navigator.goBack()
                }, 500)
            }
        })
    }

    override fun createView(context: Context): WebCommunicationScreenView {
        return WebCommunicationScreenView(context, pinCode)
    }

    fun cleanUp() {
        webServer?.stop()
    }
}


class WebCommunicationScreenView(context: Context, pinCode: String) : BaseScreenView<WebCommunicationScreen>(context) {
    var ipAddress: TextView

    init {
        View.inflate(context, R.layout.view_web_communication, this)
        ipAddress = findViewById(R.id.text_ip_address)
        ipAddress.setText(pinCode)
    }


}