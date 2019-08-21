package hedera.hgc.hgcwallet.hapi.tasks

import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.crypto.EDBip32KeyChain
import hedera.hgc.hgcwallet.crypto.EDKeyChain
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.crypto.KeyPair
import hedera.hgc.hgcwallet.hapi.APIBaseTask
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.KeyDerivation

class DetectWalletTask(private val hgcSeed: HGCSeed, private val accountID: HGCAccountID) : APIBaseTask() {

    var keyDerivation: KeyDerivation? = null

    override fun main() {
        super.main()
        val bip32Key = EDBip32KeyChain(hgcSeed).keyAtIndex(0)
        val customKey = EDKeyChain(hgcSeed).keyAtIndex(0)
        try {
            getAccountInfo(customKey)
            keyDerivation = KeyDerivation.HGC
        } catch (e: Exception) {
            error = getMessage(e)
            try {
                getAccountInfo(bip32Key)
                keyDerivation = KeyDerivation.BIP32
            } catch (e: Exception) {
                error = getMessage(e)
            }
        }
    }


    @Throws(Exception::class)
    private fun getAccountInfo(keyPair: KeyPair): Boolean {
        try {
            val response = grpc.getAccountInfo(keyPair, accountID)
            log(response.toString())
            val status = response.cryptoGetInfo.header.nodeTransactionPrecheckCode
            when (status) {
                ResponseCodeEnum.OK -> return true
                else -> throw Exception(Singleton.getErrorMessage(status))
            }
        } catch (e: Exception) {
            log("Exception : ${getMessage(e)}")
            throw e
        }

    }

}