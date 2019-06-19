package opencrowd.hgc.hgcwallet;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

import opencrowd.hgc.hgcwallet.crypto.EDKeyPair;
import opencrowd.hgc.hgcwallet.crypto.Reference;
import opencrowd.hgc.hgcwallet.crypto.bip39.Mnemonic;


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

    }
}