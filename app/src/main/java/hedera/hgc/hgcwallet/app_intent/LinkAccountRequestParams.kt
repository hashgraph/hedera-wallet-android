package hedera.hgc.hgcwallet.app_intent

import android.net.Uri

import org.json.JSONObject

class LinkAccountRequestParams(val callback: Uri, val redirect: Uri?) {
    companion object {
        fun from(jsonObject: JSONObject?): LinkAccountRequestParams? {
            if (jsonObject != null) {
                val action = jsonObject.optString("action", "")
                if (action == "requestPublicKey") {
                    val callback = jsonObject.optString("callback", "")
                    val redirect = jsonObject.optString("redirect", "")
                    val callbackUri = Uri.parse(callback)
                    val redirectUri = Uri.parse(redirect)
                    if (callback != null) {
                        return LinkAccountRequestParams(callbackUri, redirectUri)
                    }
                }

            }
            return null
        }
    }
}










