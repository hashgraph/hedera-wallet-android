package hedera.hgc.hgcwallet

object Config {
    var nodeListFileName = if (BuildConfig.USE_TEST_NET) "nodes-testnet.json" else "nodes-mainnet.json"
    var termsFile = "terms.txt"
    var privacyFile = "privacy.txt"
    var isLoggingEnabled = true
    var useBetaAPIs = true // SignatureMap and bodyBytes
    var portalFAQRestoreAccount = "https://help.hedera.com/hc/en-us/articles/360000714658"
    var defaultFee: Long = 50000000
    var defaultPort: Int = 50211
    var fileNumAddressBook = 101L
    const val termsAndConditions = "https://www.hedera.com/terms"
    const val privacyPolicy = "https://www.hedera.com/privacy"
    const val maxAllowedMemoLength = 100
    const val passcodeLength = 6
}