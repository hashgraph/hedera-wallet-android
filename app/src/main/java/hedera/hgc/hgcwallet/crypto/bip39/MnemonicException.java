/*
* There are methods in this codebase that uses references from a few
* Apache License 2.0 attribution repository below is the lists
* https://github.com/bitcoinj/bitcoinj
* core/src/main/java/org/bitcoinj/crypto/MnemonicCode.java
*/

package hedera.hgc.hgcwallet.crypto.bip39;

/**
 * Exceptions thrown by the Mnemonic module.
 */
@SuppressWarnings("serial")
public class MnemonicException extends Exception {
    public MnemonicException() {
        super();
    }

    public MnemonicException(String msg) {
        super(msg);
    }

    /**
     * Thrown when an argument to Mnemonic is the wrong length.
     */
    public static class MnemonicLengthException extends MnemonicException {
        public MnemonicLengthException(String msg) {
            super(msg);
        }
    }

    /**
     * Thrown when a list of Mnemonic words fails the checksum check.
     */
    public static class MnemonicChecksumException extends MnemonicException {
        public MnemonicChecksumException() {
            super();
        }
    }

    /**
     * Thrown when a word is encountered which is not in the Mnemonic's word list.
     */
    public static class MnemonicWordException extends MnemonicException {
        /** Contains the word that was not found in the word list. */
        public final String badWord;

        public MnemonicWordException(String badWord) {
            super();
            this.badWord = badWord;
        }
    }
}
