package hedera.hgc.hgcwallet.crypto;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;

import hedera.hgc.hgcwallet.crypto.bip39.Mnemonic;
import hedera.hgc.hgcwallet.crypto.bip39.MnemonicException;

public class HGCSeed {

    public static int bip39WordListSize = 24;
    private byte[] entropy; // 32 Bytes

    public HGCSeed(byte[] entropy) {
        this.entropy = entropy;
    }

    public HGCSeed(List<String> mnemonic) throws Exception {
        if (mnemonic.size() == HGCSeed.bip39WordListSize) {
            this.entropy = new Mnemonic().toEntropy(mnemonic);
        } else  {
            Reference reference = new Reference(TextUtils.join(" ", mnemonic));
            this.entropy = reference.toBytes();
        }
    }

    @NonNull
    public List<String> toWordsList(){
        try {
            return new Mnemonic().toMnemonic(entropy);
        } catch (MnemonicException.MnemonicLengthException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getEntropy() {
        return entropy;
    }
}
