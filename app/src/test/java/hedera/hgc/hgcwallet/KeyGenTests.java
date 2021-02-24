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

package hedera.hgc.hgcwallet;

import com.google.common.io.BaseEncoding;
import hedera.hgc.hgcwallet.common.Singleton;
import hedera.hgc.hgcwallet.crypto.EDKeyChain;
import hedera.hgc.hgcwallet.crypto.HGCSeed;
import hedera.hgc.hgcwallet.crypto.KeyChain;
import hedera.hgc.hgcwallet.crypto.KeyPair;
import hedera.hgc.hgcwallet.crypto.bip39.MnemonicException;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hedera.hgc.hgcwallet.crypto.EDKeyPair;
import hedera.hgc.hgcwallet.crypto.Reference;
import hedera.hgc.hgcwallet.crypto.SLIP10;
import hedera.hgc.hgcwallet.crypto.bip39.Mnemonic;
import hedera.hgc.hgcwallet.crypto.EDBip32KeyChain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class KeyGenTests {


    @Test
    public void edKeyPairGeneration() throws Exception {
        String seedHex = "aabbccdd11223344aabbccdd11223344aaaaaaaabbbbcc59aa2244116688bb22";
        byte[] seed = Hex.decode(seedHex);
        EDKeyPair keyPair = new EDKeyPair(seed);
        String publicKey = Hex.toHexString(keyPair.getPublicKey());
        assertEquals("720a5e6b5891e2e3226b662681c555d88b53087773d2dae9742eeb69e1aef8ad",publicKey);
        byte[] message = Hex.decode("3c147d61");
        byte[] signature = keyPair.signMessage(message);
        String signatureString = Hex.toHexString(signature);
        boolean verified = keyPair.verifySignature(message, signature);
        assertTrue(verified);
    }

    @Test
    public void seedGenerationAndKeyChain() throws Exception {
        String entropyStr = "aabbccdd11223344aabbccdd11223344aaaaaaaabbbbcc213344aaaaaaaabbbb";
        String wordsStr = "casual echo flesh tribal run react crunch cure pair sum skip fled castle floor crunch deputy run react castle fled cruise ethnic";
        byte[] entropy = Hex.decode(entropyStr);
        Reference reference = new Reference(entropy);
        String s = String.join(" ",reference.toWordsList());
        assertEquals(s,wordsStr);
        Reference rr = new Reference(wordsStr);
        String s1 = Hex.toHexString(rr.toBytes());
        assertEquals(s1,entropyStr);
    }

    @Test
    public void testbip39() throws Exception {
        String entropyStr = "aabbccdd11223344aabbccdd11223344aaaaaaaabbbbcc213344aaaaaaaabbbb";
        String wordsStr = "casual echo flesh tribal run react crunch cure pair sum skip fled castle floor crunch deputy run react castle fled cruise ethnic";

        byte[] entropy = Hex.decode(entropyStr);
        Mnemonic mnemonicCode = new Mnemonic();

        List<String> bip39WordList = mnemonicCode.toMnemonic(entropy);
        String words = String.join(" ", bip39WordList);

        byte[] conevertedEntropy = mnemonicCode.toEntropy(bip39WordList);
        assertEquals(entropyStr, Hex.toHexString(conevertedEntropy));
        assertEquals("primary taxi dance car case pelican priority kangaroo tackle math mimic matter primary fetch priority jazz slow another spell fetch primary fetch upon hip", words);
    }

    @Test
    public void testWordListFromIOS() throws Exception {
        String wordsStr1 = "glance nothing increase cancel list fence flower joy suffer offer aunt very expect ordinary rug estate silly silly select aim among kangaroo add ready";
        String wordsStr2 = "there season tumble dragon regret glad shoot ecology dignity flight major grape viable phrase kid pig drink artist tornado canal bracket velvet ketchup demise";
        String wordsStr3 = "squeeze essay exact chapter tattoo boat giraffe cable either blast pattern slice grab erupt grant useless retire drink lake ice art web already flip";
        String wordsStr4 = "sold accent loosen cactus retina slowly freely suffer habit soil tip pray stem aboard suite mad coax pagan silken tomb much gender";
        List<String> wordList = new ArrayList<String>(Arrays.asList(wordsStr4.split(" ")));

        HGCSeed seed = new HGCSeed(wordList);
        KeyPair keyPair;

        if(wordList.size() > 22) {
            KeyChain bip32KeyChain = new EDBip32KeyChain(seed);
            keyPair = bip32KeyChain.keyAtIndex(0);

        } else {
            KeyChain customKeyChain = new EDKeyChain(seed);
            keyPair = customKeyChain.keyAtIndex(0);
        }

        System.out.println(bytesToString(keyPair.getPublicKey()));
        System.out.println(bytesToString(keyPair.getPrivateKey()));
    }

    private String bytesToString(byte[] bytes) {
        return BaseEncoding.base16().lowerCase().encode(bytes);
    }

    @Test
    public void testBip39ComaptibilityWithLedgerWallet() throws Exception {
        String words = "draft struggle fitness mimic mountain rare lonely grocery topple wreck satoshi kangaroo balcony odor tiger crush bamboo parent monkey afraid elite earn hundred learn";
        byte[] seed = Mnemonic.generateSeed(words, "");
        assertEquals("60691cded1328c5799e36d72aec3842b5230d376ce9b1177b3dc8c79d2d715b099c486fbf91a93ebadcaf473fafa79d5d694c013bcc561c130c447e3f84659f4",Hex.toHexString(seed));

        byte[] edSeed =  SLIP10.deriveEd25519PrivateKey(seed, 44, 3030, 0, 0, 0);
        EDKeyPair keyPair = new EDKeyPair(edSeed);
        assertEquals("00516a26d75230616da9b18b27fa4d1ce68ca6dbb6db5ee42dc63f35c977310f", Hex.toHexString(keyPair.getPublicKey()));
    }
}