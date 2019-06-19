package opencrowd.hgc.hgcwallet.ui.scan;

public interface QRScanListener {
    void onQRScanFinished(boolean success, String result);
}
