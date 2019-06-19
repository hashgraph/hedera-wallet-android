package opencrowd.hgc.hgcwallet.export_key

import android.net.wifi.WifiManager
import android.util.Base64
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import opencrowd.hgc.hgcwallet.App
import opencrowd.hgc.hgcwallet.common.Singleton
import opencrowd.hgc.hgcwallet.database.account.Account
import org.json.JSONObject
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

class WebServer(val port: Int, val listener: WebServerListener) : NanoHTTPD(port) {
    init {
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun getWifiIP(): String? {
            val wifiManager = App.instance.getSystemService(android.content.Context.WIFI_SERVICE) as WifiManager
            var ipAddress = wifiManager.connectionInfo.ipAddress

            // Convert little-endian to big-endianif needed
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                ipAddress = Integer.reverseBytes(ipAddress)
            }

            val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()

            var ipAddressString: String?
            try {
                ipAddressString = InetAddress.getByAddress(ipByteArray).hostAddress
            } catch (ex: UnknownHostException) {
                Log.e("WIFIIP", "Unable to get host address.")
                ipAddressString = null
            }

            return ipAddressString
        }
    }

    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {

        var responseJSON: JSONObject? = null
        var errorMsg = ""
        val account = listener.getAccount()
        if (account != null) {
            val jsonStr = Singleton.accountToJSONString(account, true)
            if (jsonStr != null) {
                val data = listener.getPairingParams()?.encrypt(jsonStr.toByteArray())
                if (data != null)
                    responseJSON = JSONObject()
                            .put("data", Base64.encodeToString(data, Base64.DEFAULT))
                            .put("success", true)
                else
                    errorMsg = "Failed to encrypt the data"

            } else
                errorMsg = "Invalid Account is selected"
        } else
            errorMsg = "No Account is selected"

        if (responseJSON == null)
            responseJSON = JSONObject().put("error", errorMsg)

        val response = NanoHTTPD.newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, responseJSON.toString())
        response.addHeader("Access-Control-Allow-Origin","*");
        response.addHeader("Access-Control-Allow-Methods","PUT,POST,GET,PATCH,DELETE");
        response.addHeader("Access-Control-Allow-Credentials","true");

        listener.requestHandled()
        return response
    }
}

interface WebServerListener {
    fun getPairingParams(): PairingParams?
    fun getAccount(): Account?
    fun requestHandled(success: Boolean = true)

}