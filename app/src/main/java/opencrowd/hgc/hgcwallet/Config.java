package opencrowd.hgc.hgcwallet;

public class Config {
    public static String nodeListFileName = BuildConfig.USE_TEST_NET ? "nodes-testnet.json" : "nodes-mainnet.json";
    public static String termsFile = "terms.txt";
    public static String privacyFile = "privacy.txt";
    public static boolean isLoggingEnabled = true;
    public static boolean useBetaAPIs = false; // SignatureMap and bodyBytes
}
