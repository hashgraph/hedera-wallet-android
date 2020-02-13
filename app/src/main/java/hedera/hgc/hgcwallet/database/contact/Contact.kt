package hedera.hgc.hgcwallet.database.contact

import androidx.room.Entity
import androidx.room.PrimaryKey
import hedera.hgc.hgcwallet.database.Converters


@Entity
data class Contact(@PrimaryKey var accountId: String, var name: String?, var isVerified: Boolean = false, var metaData: String = "") {

    fun isThirdPartyContact(): Boolean {
        val host = getHost()
        return (host != null && host.isNotBlank())
    }

    fun getHost(): String? {
        return if (metaData.isNotBlank()) {
            val metaDataMap = Converters().fromString(metaData)
            metaDataMap.get("host")
        } else null
    }
}
