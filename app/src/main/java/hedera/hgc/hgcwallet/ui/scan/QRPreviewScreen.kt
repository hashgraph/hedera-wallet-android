package hedera.hgc.hgcwallet.ui.scan

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class QRPreviewScreen(qrCode: String) : Screen<QRPreviewView>() {

    data class Params(val qrString: String, val bitmap: Bitmap?)

    private val param: Params

    init {
        param = Params(qrCode, generateQRImage(qrCode))
    }

    override fun createView(context: Context): QRPreviewView {
        return QRPreviewView(context, param)
    }

    private fun generateQRImage(qrCode: String): Bitmap? {
        var bmp: Bitmap? = null
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(qrCode, BarcodeFormat.QR_CODE, 700, 700)
            val barcodeEncoder = BarcodeEncoder()
            bmp = barcodeEncoder.createBitmap(bitMatrix)

        } catch (e: WriterException) {
            e.printStackTrace()
        }

        return bmp
    }

    internal fun goBack() {
        navigator?.goBack()
    }

}

class QRPreviewView(context: Context, val param: QRPreviewScreen.Params) : BaseScreenView<QRPreviewScreen>(context) {
    private val titleBarWrapper: TitleBarWrapper
    private val qrCodeImage: ImageView?
    private val qrCodeDescription: TextView?

    init {
        View.inflate(context, R.layout.view_qr_preview, this)
        titleBarWrapper = TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener { screen?.goBack() }
            setTitle("Create Account Request")
        }
        qrCodeImage = findViewById(R.id.imageView_qrcode)
        qrCodeDescription = findViewById(R.id.text_qrcode_description)

        reloadData()
    }

    fun reloadData() {
        qrCodeDescription?.text = param.qrString
        param.bitmap?.let {
            qrCodeImage?.setImageBitmap(it)
        }

    }
}