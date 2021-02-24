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

package hedera.hgc.hgcwallet.crypto;

import android.os.Build;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;
import java.util.prefs.InvalidPreferencesFormatException;

import androidx.annotation.RequiresApi;
import hedera.hgc.hgcwallet.crypto.bip39.Mnemonic;
import hedera.hgc.hgcwallet.crypto.bip39.MnemonicException;

public class HGCSeed {

    public static int bip39WordListSize = 24;
    private byte[] entropy; // 32 Bytes

    public HGCSeed(byte[] entropy) {
        this.entropy = entropy;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public HGCSeed(List<String> mnemonic) throws Exception {
        if (mnemonic.size() == HGCSeed.bip39WordListSize) {
            this.entropy = new Mnemonic().toEntropy(mnemonic);
        } else  {
            Reference reference = new Reference(String.join(" ", mnemonic));
            this.entropy = reference.toBytes();
        }
    }

    @NonNull
    public List<String> toWordsList(){
        try {
            return new Reference(entropy).toWordsList();
//            return new Mnemonic().toMnemonic(entropy);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getEntropy() {
        return entropy;
    }
}
