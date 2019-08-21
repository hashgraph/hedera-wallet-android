package hedera.hgc.hgcwallet.crypto;

import android.support.annotation.NonNull;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import java.security.MessageDigest;
import java.security.Signature;


public class EDKeyPair implements KeyPair {

    private EdDSAPrivateKey privateKey;
    private EdDSAPublicKey publicKey;

    public EDKeyPair(@NonNull byte[] seed) {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        EdDSAPrivateKeySpec privateKeySpec = new EdDSAPrivateKeySpec(seed, spec);
        this.privateKey = new EdDSAPrivateKey(privateKeySpec);
        EdDSAPublicKeySpec pubKeySpec = new EdDSAPublicKeySpec(privateKeySpec.getA(), spec);
        this.publicKey = new EdDSAPublicKey(pubKeySpec);
    }

    @NonNull
    @Override
    public byte[] getPrivateKey() {
        byte[] seed = privateKey.getSeed();
        byte[] publicKey = getPublicKey();

        byte[] key = new byte[seed.length + publicKey.length];
        System.arraycopy(seed, 0, key, 0, seed.length);
        System.arraycopy(publicKey, 0, key, seed.length, publicKey.length);
        return key;
    }

    @NonNull
    @Override
    public byte[] getPublicKey() {
        return publicKey.getAbyte();
    }

    @NonNull
    @Override
    public byte[] signMessage(@NonNull byte[] message) {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        try {
            Signature sgr = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
            sgr.initSign(privateKey);
            sgr.update(message);
            return  sgr.sign();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public boolean verifySignature(@NonNull byte[] message, byte[] signature) {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        try {
            Signature sgr = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
            sgr.initVerify(publicKey);
            sgr.update(message);
            return sgr.verify(signature);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
