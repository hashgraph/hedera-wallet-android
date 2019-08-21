package hedera.hgc.hgcwallet.app_intent

import android.net.Uri
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.PublicKeyAddress
import org.json.JSONObject

class LinkAccountParams private constructor(
        val accountID: HGCAccountID,
        val address: PublicKeyAddress,
        val redirect: Uri
) {

    companion object {
        fun from(jsonObject: JSONObject?): LinkAccountParams? {
            var accountParams: LinkAccountParams? = null
            jsonObject?.let { obj ->
                when (obj.optString("action", "")) {
                    "recvAccountId", "setAccountId" -> {
                        PublicKeyAddress.from(obj.optString("publicKey", ""))?.let { pk ->
                            val realmNum = obj.optLong("realmNum", 0L)
                            val shardNum = obj.optLong("shardNum", 0L)
                            val accountNum = obj.optLong("accountNum", 0L)
                            val redirect = obj.optString("redirect", "")
                            val redirectUri = Uri.parse(redirect)
                            if (realmNum != 0L || shardNum != 0L || accountNum != 0L)
                                accountParams = LinkAccountParams(HGCAccountID(realmNum, shardNum, accountNum), pk, redirectUri)
                        }

                    }
                    else -> Unit
                }
            }
            return accountParams
        }
    }

}