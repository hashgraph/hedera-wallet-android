package hedera.hgc.hgcwallet.crypto;

import android.support.annotation.NonNull;

import org.spongycastle.asn1.pkcs.PBKDF2Params;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CryptoUtils {
    @NonNull
    public static byte[] getSecureRandomData(int length){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    @NonNull
    public static byte[] sha256Digest(@NonNull byte[] message) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(message);
        return hash;
    }

    @NonNull
    public static byte[] sha384Digest(@NonNull byte[] message) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-384");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(message);
        return hash;
    }

    @NonNull
    public static byte[] deriveKey(@NonNull byte[] seed, long index, int length) {

        byte[] password = new byte[seed.length + Long.BYTES];
        for (int i = 0; i < seed.length; i++) {
            password[i] = seed[i];
        }
        byte[] indexData = longToBytes(index);
        int c = 0;
        for (int i = indexData.length-1; i >=0 ; i--) {
            password[seed.length + c] = indexData[i];
            c++;
        }

        byte[] salt = new byte[]{-1};;

        String passwordStr = Hex.toHexString(password);
        PBKDF2Params params = new PBKDF2Params(salt,2048, length*8);

        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA512Digest());
        gen.init(password, params.getSalt(), params.getIterationCount().intValue());

        byte[] derivedKey = ((KeyParameter)gen.generateDerivedParameters(length*8)).getKey();

        return derivedKey;
    }

    @NonNull
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
}
