package hedera.hgc.hgcwallet.modals

import org.spongycastle.util.encoders.Hex

class PublicKeyAddress private constructor(val hexAddress: String) {

    fun stringRepresentation(): String {
        return hexAddress
    }

    fun getByteArray(): ByteArray {
        return Hex.decode(hexAddress)
    }

    companion object {
        val lengthED = 32
        fun from(str: String): PublicKeyAddress? {
            return try {
                if (str.isBlank() ||  Hex.decode(str).let { it == null || it.size != lengthED }) null else PublicKeyAddress(str)
            } catch (e: Exception) {
                null
            }
        }
    }
}