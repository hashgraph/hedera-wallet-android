package opencrowd.hgc.hgcwallet.crypto;

public interface KeyPair {
    byte[] getPrivateKey();
    byte[] getPublicKey();
    byte[] signMessage(byte[] message);
    boolean verifySignature(byte[] message, byte[] signature);
}
