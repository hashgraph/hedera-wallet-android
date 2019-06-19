package opencrowd.hgc.hgcwallet.modals;


import org.spongycastle.util.encoders.Hex;

public class PublicKeyAddress {
    String hexAddress;
    private PublicKeyAddress(String hexAddress) {
        this.hexAddress = hexAddress;
    }
    public static PublicKeyAddress from(String string) {
        try {
            if (string == null || string.isEmpty() || Hex.decode(string) == null) return null;
            return new PublicKeyAddress(string);
        } catch (Exception e) {
            return null;
        }
    }

    public String stringRepresentation() {
        return hexAddress;
    }
}
