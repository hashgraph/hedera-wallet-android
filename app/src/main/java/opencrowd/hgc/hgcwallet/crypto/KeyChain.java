package opencrowd.hgc.hgcwallet.crypto;

import android.support.annotation.NonNull;

public interface KeyChain {
    @NonNull
    KeyPair keyAtIndex(long index);
}