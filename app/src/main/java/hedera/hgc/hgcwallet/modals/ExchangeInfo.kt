package hedera.hgc.hgcwallet.modals

data class ExchangeInfo(var accountId: HGCAccountID, var name: String, var host: String, var memo: String?) {

    companion object {

        fun fromQRCode(code: String): ExchangeInfo? {
            val components = code.split(",")
            return if (components.size > 3) {
                val memo = if (components.size > 4) components[4] else null
                HGCAccountID.fromString(components[3])?.let {
                    ExchangeInfo(it, components[0], "${components[1]}:${components[2]}", memo)
                }

            } else
                null
        }

        fun toHttpUrl(host: String): String {
            return if (host.startsWith("http://", true) || host.startsWith("https://", true))
                host
            else
                "http://$host"
        }
    }
}