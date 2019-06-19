package opencrowd.hgc.hgcwallet.app_intent;

import android.support.annotation.NonNull;

import opencrowd.hgc.hgcwallet.BuildConfig;
import opencrowd.hgc.hgcwallet.Config;

public interface IntentParams {
    public static final @NonNull String APP_URL_SCHEMA = "https";
    public static final @NonNull String APP_HOST = BuildConfig.USE_TEST_BRANCHIO ? "hedera.test-app.link" : "hedera.app.link";
    public static final @NonNull String APP_URL_PATH = BuildConfig.USE_TEST_BRANCHIO ? "OYEncK9sLQ" : "5vuEEQhtLQ";
}
