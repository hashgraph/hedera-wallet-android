package hedera.hgc.hgcwallet.app_intent

import android.net.Uri
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.PublicKeyAddress
import org.json.JSONObject
import java.net.URI

class CreateAccountRequestParams private constructor(
        var publicKeyAddress: PublicKeyAddress,
        var initialAmount: Long?
) : IntentParams, UriConvertible, QRConvertible {

    override fun asUri(): Uri {
        val builder = Uri.Builder()
        builder.scheme(IntentParams.APP_URL_SCHEMA)
                .authority(IntentParams.APP_HOST)
                .appendPath(IntentParams.APP_URL_PATH)
                .appendQueryParameter("action", action)
                .appendQueryParameter("pubKey", publicKeyAddress.stringRepresentation())
        initialAmount?.let {
            builder.appendQueryParameter("a", it.toString())
        }
        return builder.build()
    }

    override fun asQRCode(): String = asUri().toString()

    companion object {
        private val action = "caRequest"
        fun from(publicKeyAddress: PublicKeyAddress, initialAmount: Long? = null): CreateAccountRequestParams {
            return CreateAccountRequestParams(publicKeyAddress, initialAmount)
        }


        fun from(uri: Uri): CreateAccountRequestParams? {
            val actionVal = uri.getQueryParameter("action")
            return if (actionVal != null && actionVal == action) {
                uri.getQueryParameter("pubKey")?.let {
                    PublicKeyAddress.from(it)?.let {
                        CreateAccountRequestParams(it, uri.getQueryParameter("a")?.toLongOrNull())
                    }
                }
            } else null

        }

        fun from(qrCode: String): CreateAccountRequestParams? {
            var result = PublicKeyAddress.from(qrCode)?.let {
                from(it)
            }

            if (result == null) {
                result = Uri.parse(qrCode)?.let {
                    from(it)
                }

            }

            return result
        }

        fun from(jsonObject: JSONObject): CreateAccountRequestParams? {
            return if (jsonObject.optString("action", "") == action) {
                PublicKeyAddress.from(jsonObject.optString("pubKey", ""))?.let {
                    CreateAccountRequestParams(it, jsonObject.optString("a", "").toLongOrNull())
                }
            } else null
        }

    }
}