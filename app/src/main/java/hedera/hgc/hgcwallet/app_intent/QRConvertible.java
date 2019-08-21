package hedera.hgc.hgcwallet.app_intent;

import android.support.annotation.NonNull;

public interface QRConvertible {
    @NonNull
    public String asQRCode();
}
