package hedera.hgc.hgcwallet.ui.scan

interface QRScanListener {
    fun onQRScanFinished(success: Boolean, result: String?)
}
