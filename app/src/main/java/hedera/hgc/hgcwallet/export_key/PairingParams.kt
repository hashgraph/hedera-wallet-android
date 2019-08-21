package hedera.hgc.hgcwallet.export_key

import hedera.hgc.hgcwallet.crypto.CryptoUtils
import org.spongycastle.util.encoders.Hex
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class PairingParams(val key: ByteArray, val ip: String) {

    companion object {
        fun fromQRCode(code: String): PairingParams? {
            return try {
                val list = code.split("\n")
                 if (list.size > 1) {
                     val key = Hex.decode(list[0])
                     val ipComponents = list[1].split(".")
                     if ((key.size == 16 || key.size == 32 ) && ipComponents.size == 4) {
                         PairingParams(key, list[1])
                     } else {
                         null
                     }

                } else null
            } catch (e:java.lang.Exception) {
                  null
            }

        }
    }

    fun encrypt(data: ByteArray): ByteArray? {
        val aes = AESCTREncryption(key)
        try {
            val digest = CryptoUtils.sha384Digest(data)
            val fullMessage = data + digest
            return aes.encryptionCipher.doFinal(fullMessage)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun decrypt(data: ByteArray): ByteArray? {
        val aes = AESCTREncryption(key)
        try {
            return aes.decryptionCipher.doFinal(data)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun getPIN(serverIp: String): String {
        val serverIpArr = serverIp.split(".")
        val extensionIpArr = ip.split(".")
        var codeArr = serverIpArr
        if (extensionIpArr.size == serverIpArr.size)
            for (i in serverIpArr.indices)
                if (serverIpArr[i] != extensionIpArr[i]) {
                    codeArr = serverIpArr.subList(i, serverIpArr.size)
                    break
                }


        return codeArr.joinToString("A")
    }

}

internal class AESCTREncryption(private val key: ByteArray) {
    private val cypherInstance = "AES/CTR/NoPadding"
    private val initializationVector = Hex.decode("00000000000000000000000000000000")

    val encryptionCipher: Cipher
        @Throws(Exception::class)
        get() {

            val skeySpec = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance(cypherInstance)
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(initializationVector))
            return cipher
        }

    val decryptionCipher: Cipher
        @Throws(Exception::class)
        get() {
            try {
                val skeySpec = SecretKeySpec(key, "AES")
                val cipher = Cipher.getInstance(cypherInstance)
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, IvParameterSpec(initializationVector))
                return cipher
            } catch (e: Exception) {
                throw e
            }

        }
}