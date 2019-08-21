package hedera.hgc.hgcwallet

object Config {
    var nodeListFileName = if (BuildConfig.USE_TEST_NET) "nodes-testnet.json" else "nodes-mainnet.json"
    var termsFile = "terms.txt"
    var privacyFile = "privacy.txt"
    var isLoggingEnabled = true
    var useBetaAPIs = true // SignatureMap and bodyBytes
    var portalFAQRestoreAccount = "https://help.hedera.com/hc/en-us/articles/360000714658"
    var defaultFee: Long = 100000000
    var fileNumAddressBook = 101L
}