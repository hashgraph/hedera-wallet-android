package hedera.hgc.hgcwallet.app_intent;

import android.net.Uri;
import android.support.annotation.NonNull;

public interface UriConvertible {
    @NonNull
    public Uri asUri();
}
