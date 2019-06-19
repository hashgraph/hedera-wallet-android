package opencrowd.hgc.hgcwallet.local_auth

import opencrowd.hgc.hgcwallet.common.AppLog
import opencrowd.hgc.hgcwallet.common.UserSettings
import opencrowd.hgc.hgcwallet.crypto.CryptoUtils
import opencrowd.hgc.hgcwallet.crypto.HGCSeed
import org.spongycastle.asn1.pkcs.PBKDF2Params
import org.spongycastle.crypto.digests.SHA512Digest
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.spongycastle.crypto.params.KeyParameter
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AuthManager {
    private val TAG = "AuthManager"
    private var aes: AESEncyption? = null

    private var appOnPauseTime: Date? = null
    private var appOnResumeTime: Date? = null
    private val maxAllowedBackgroundStayTime = 120 // in seconds

    var authType = AuthType.UNKNOWN
        get() {
            if (hasFingerprintSetup())
                return AuthType.FINGER
            else if  (hasPinSetup())
                return AuthType.PIN
            else {
                // Migration
                val type = UserSettings.instance.getValue(UserSettings.KEY_AUTH_TYPE)
                if (type != null) {
                    if (type == "custom")
                        return AuthType.PIN
                    if (type == "device")
                        return AuthType.FINGER

                }
            }
            return AuthType.UNKNOWN
        }

    fun hasAuth(): Boolean {
        var backgroundStayTime: Long = 0

        if (appOnPauseTime != null && appOnResumeTime != null) {
            val diff = appOnResumeTime!!.time - appOnPauseTime!!.time
            backgroundStayTime = diff / 1000
        }

        return aes != null && backgroundStayTime >= 0 && backgroundStayTime <= maxAllowedBackgroundStayTime
    }

    fun hasFingerprintSetup():Boolean {
        return SecureStorage.hasValue(getSecureStorageKey(AuthType.FINGER))
    }

    fun hasPinSetup():Boolean {
        return SecureStorage.hasValue(getSecureStorageKey(AuthType.PIN))
    }

    fun handleActivityDidPause() {
        appOnPauseTime = Date()
    }

    fun handleActivityDidResume() {
        appOnResumeTime = Date()
    }

    private fun saveDataEncryptionKey(enCipher: Cipher, key: ByteArray, authType: AuthType) {
        SecureStorage.setValue(getSecureStorageKey(authType), key, enCipher)
    }

    private fun getSecureStorageKey(authType: AuthType) = when (authType) {
        AuthType.FINGER ->  SecureStorage.KEY_DATA_ENCRYPTION_KEY_FINGERPRINT
        AuthType.PIN -> SecureStorage.KEY_DATA_ENCRYPTION_KEY_PIN
        AuthType.UNKNOWN -> ""
    }

    private fun getRandomDataEncryptionKey() = AESEncyption(CryptoUtils.getSecureRandomData(AESEncyption.keySizeInBytes))

    private fun setEnCipher(cipher: Cipher, a: AuthType) {
        if (aes == null)
            aes = getRandomDataEncryptionKey()

        aes?.let {
            saveDataEncryptionKey(cipher, it.key, a)
        }
        authType = a
    }

    private fun setDrCipher(cipher: Cipher, a: AuthType): Boolean {
        try {
            SecureStorage.getValue(getSecureStorageKey(a), cipher)?.let {
                aes = AESEncyption(it)
                return true
            }
            return false
        } catch (e: Exception) {
            AppLog.e(TAG, "setDrCipher : ${e.message ?: ""} ", e.cause)
            return false
        }
    }

    @Throws(Exception::class)
    fun setPIN(pin: String) {
        val key = deriveAESKey(pin, AESEncyption.keySize)
        val pinAes = AESEncyption(key)

        setEnCipher(pinAes.encryptionCipher, AuthType.PIN)
        disableFingerprintAuth()
    }

    @Throws(Exception::class)
    fun verifyPIN(pin: String): Boolean {
        val key = deriveAESKey(pin, AESEncyption.keySize)
        val pinAes = AESEncyption(key)
        val cipher = pinAes.decryptionCipher
        val isValid = setDrCipher(cipher, AuthType.PIN)

        if (!isValid) {
            // Check for migration
            val sb = SecureStorage.getValue(SecureStorage.KEY_HGC_SEED, cipher)
            sb?.let {
                setPIN(pin)
                saveSeed(it)
                return true
            }
        }
        return isValid
    }

    fun setFingerprintAuth(cipher: Cipher) {
        setEnCipher(cipher, AuthType.FINGER)
        SecureStorage.clearValue(getSecureStorageKey(AuthType.PIN))
    }

    fun verifyFingerPrintAuth(cipher: Cipher): Boolean {
        return setDrCipher(cipher, AuthType.FINGER)
    }

    fun verifyFingerPrintAuthOld(cipher: Cipher): Boolean {
        // Check for migration
        val seedBytes = SecureStorage.getValue(SecureStorage.KEY_HGC_SEED, cipher)
        seedBytes?.let {
            if (aes == null)
                aes = getRandomDataEncryptionKey()
            saveSeed(it)
            return true
        }

        return false
    }

    fun disableFingerprintAuth() {
        SecureStorage.clearValue(getSecureStorageKey(AuthType.FINGER))
    }


    fun deriveAESKey(passwordStr: String, length: Int): ByteArray {

        val password = passwordStr.toByteArray()
        val salt = byteArrayOf(-1)
        val params = PBKDF2Params(salt, 10000, length)

        val gen = PKCS5S2ParametersGenerator(SHA512Digest())
        gen.init(password, params.getSalt(), params.getIterationCount().toInt())
        return (gen.generateDerivedParameters(length) as KeyParameter).getKey()
    }


    fun encrypt(message: ByteArray): ByteArray? {
        return aes?.encrypt(message)
    }

    fun decrypt(encryptedMessage: ByteArray): ByteArray? {
        return aes?.decrypt(encryptedMessage)
    }

    fun getDecryptionCipher() = aes?.decryptionCipher
    fun getEncryptionCipher() = aes?.encryptionCipher

    fun getSeed():HGCSeed? {
        getDecryptionCipher()?.let {
            val data = SecureStorage.getValue(SecureStorage.KEY_SEED, it)
            if (data != null)
                return  HGCSeed(data)
        }
        return null
    }

    fun saveSeed(seedBytes: ByteArray) {
        getEncryptionCipher()?.let {
            SecureStorage.setValue(SecureStorage.KEY_SEED, seedBytes, it)
        }
    }

}

private class AESEncyption(public val key: ByteArray) {
    companion object {
        private val TAG = "AuthManager.AESEncryption"
        private val cypherInstance = "AES/CBC/PKCS5Padding"
        private val initializationVector = "8119745113154120"
        val keySize = 256
        val keySizeInBytes = 32
    }

    val encryptionCipher: Cipher
        @Throws(Exception::class)
        get() {

            val skeySpec = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance(cypherInstance)
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(initializationVector.toByteArray()))
            return cipher
        }

    val decryptionCipher: Cipher
        @Throws(Exception::class)
        get() {
            try {
                val skeySpec = SecretKeySpec(key, "AES")
                val cipher = Cipher.getInstance(cypherInstance)
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, IvParameterSpec(initializationVector.toByteArray()))
                return cipher
            } catch (e: Exception) {
                AppLog.e(TAG, "decryptionCipher : ${e.message ?: ""} ", e.cause)
                throw e
            }

        }

    fun encrypt(message: ByteArray): ByteArray? {
        var result: ByteArray? = null
        try {
            result = encryptionCipher.doFinal(message)
        } catch (e: Exception) {
            AppLog.e(TAG, "encrypt : ${e.message ?: ""} ", e.cause)
        }

        return result
    }

    fun decrypt(encryptedMessage: ByteArray): ByteArray? {
        var result: ByteArray? = null
        try {
            result = decryptionCipher.doFinal(encryptedMessage)
        } catch (e: Exception) {
            AppLog.e(TAG, "decrypt : ${e.message ?: ""} ", e.cause)
        }

        return result
    }
}


enum class AuthType(val value: String) {
    PIN("pin"),
    FINGER("finger"),
    UNKNOWN("unknown")
}

interface AuthListener {
    fun onAuthSetupSuccess()
    fun onAuthSuccess(requestCode: Int)
    fun onAuthSetupFailed(isCancelled: Boolean)
    fun onAuthFailed(requestCode: Int, isCancelled: Boolean)
}

