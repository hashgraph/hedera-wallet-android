package hedera.hgc.hgcwallet.app_intent;

import android.support.annotation.NonNull;

import hedera.hgc.hgcwallet.BuildConfig;

public interface IntentParams {
    public static final @NonNull String APP_URL_SCHEMA = "https";
    public static final @NonNull String APP_HOST = BuildConfig.USE_TEST_BRANCHIO ? "hedera.test-app.link" : "hedera.app.link";
    public static final @NonNull String APP_URL_PATH = BuildConfig.USE_TEST_BRANCHIO ? "OYEncK9sLQ" : "5vuEEQhtLQ";
}
