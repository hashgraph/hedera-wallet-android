package hedera.hgc.hgcwallet.app_intent

import hedera.hgc.hgcwallet.BuildConfig

interface IntentParams {
    companion object {
        val APP_URL_SCHEMA = "https"
        val APP_HOST = if (BuildConfig.USE_TEST_BRANCHIO) "hedera.test-app.link" else "hedera.app.link"
        val APP_URL_PATH = if (BuildConfig.USE_TEST_BRANCHIO) "OYEncK9sLQ" else "5vuEEQhtLQ"
    }
}
