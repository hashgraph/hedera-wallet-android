package opencrowd.hgc.hgcwallet.local_auth


import opencrowd.hgc.hgcwallet.common.AppLog
import opencrowd.hgc.hgcwallet.common.UserSettings
import org.spongycastle.util.encoders.Hex
import javax.crypto.Cipher

object SecureStorage {
    private val TAG = "SecureStorage"
    var KEY_DATA_ENCRYPTION_KEY_FINGERPRINT = "key-data-encryption-key-fingerprint"
    var KEY_DATA_ENCRYPTION_KEY_PIN = "key-data-encryption-key-pin"
    var KEY_SEED = "key-seed"
    var KEY_HGC_SEED = "hgc-seed"

    private fun encrypt(message: ByteArray, cipher: Cipher): EncryptionResult? {
        var result: EncryptionResult? = null
        try {
            result = EncryptionResult(cipher.doFinal(message), cipher.iv)
        } catch (e: Exception) {
            AppLog.e(TAG, "encrypt : ${e.message?:""} ", e.cause)
            e.printStackTrace()
        }

        return result
    }

    private fun decrypt(encryptedMessage: ByteArray, cipher: Cipher): ByteArray? {
        var result: ByteArray? = null
        try {
            result = cipher.doFinal(encryptedMessage)
        } catch (e: Exception) {
            AppLog.e(TAG, "decrypt : ${e.message?:""} ", e.cause)
            e.printStackTrace()
        }

        return result
    }

    fun hasValue(key: String): Boolean {
        val keyMsg = "$key-m"
        UserSettings.instance.getValue(keyMsg)?.let {
            return it.isNotEmpty()
        }
        return false
    }

    fun clearValue(key: String) {
        val keyMsg = "$key-m"
        val keyIV = "$key-iv"
        UserSettings.instance.resetValue(keyMsg)
        UserSettings.instance.resetValue(keyIV)
    }

    fun getValue(key: String, cipher: Cipher): ByteArray? {
        var result: ByteArray? = null
        val keyMsg = "$key-m"
        UserSettings.instance.getValue(keyMsg)?.let {
            result = decrypt(Hex.decode(it), cipher)
        }
        return result
    }

    fun getIV(key: String): ByteArray? {
        var result: ByteArray? = null
        val keyIV = "$key-iv"
        UserSettings.instance.getValue(keyIV)?.let {
            result = Hex.decode(it)
        }

        return result
    }

    fun setValue(key: String, msg: ByteArray, cipher: Cipher) {
        val result = encrypt(msg, cipher)
        val hexData = Hex.toHexString(result!!.encryptedData)
        val hexIV = Hex.toHexString(result.IV)
        val keyMsg = "$key-m"
        val keyIV = "$key-iv"
        UserSettings.instance.setValue(keyMsg, hexData)
        UserSettings.instance.setValue(keyIV, hexIV)
    }

    class EncryptionResult(var encryptedData: ByteArray, var IV: ByteArray)
}
