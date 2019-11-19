/*
 *
 *  Copyright 2019 Hedera Hashgraph LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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

    const val  bitrexURL = "https://api.bittrex.com/api/v1.1/public/getticker?market=USD-HBAR"
    const val  liquidURL = "https://api.liquid.com/products/557"
    const val  okcoinURL = "https://www.okcoin.com/api/spot/v3/instruments/HBAR-USD/ticker"
}