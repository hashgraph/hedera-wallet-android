package hedera.hgc.hgcwallet.hapi.tasks

import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.crypto.KeyPair
import hedera.hgc.hgcwallet.hapi.APIBaseTask
import hedera.hgc.hgcwallet.hapi.APIRequestBuilder
import hedera.hgc.hgcwallet.modals.HGCAccountID
import io.grpc.StatusRuntimeException

class MigrateToBip32Task(private val oldKey: KeyPair, private val newKey: KeyPair, private val accountID: HGCAccountID, private val verifyOnly: Boolean = false) : APIBaseTask() {


    var migrationStatus = MigrateStatus.Initial

    init {
        grpc.timeout = 10
    }

    override fun main() {
        super.main()

        try {
            if (!verifyOnly)
                updateAccount(oldKey, newKey)

            getAccountInfo(newKey)
            migrationStatus = MigrateStatus.Success

        } catch (e: Exception) {
            log("Exception : ${getMessage(e)}")
            error = getMessage(e)
            if (migrationStatus == MigrateStatus.Initial && e is StatusRuntimeException)
                migrationStatus = MigrateStatus.Failed_Network
        }
    }

    @Throws(Exception::class)
    private fun getAccountInfo(keyPair: KeyPair): Boolean {
        val response = grpc.getAccountInfo(keyPair, accountID)
        log(response.toString())
        val status = response.cryptoGetInfo.header.nodeTransactionPrecheckCode
        when (status) {
            ResponseCodeEnum.OK -> return true
            else -> {
                migrationStatus = MigrateStatus.Failed_Verfiy_Key_Update
                throw Exception(Singleton.getErrorMessage(status))
            }
        }

    }

    @Throws(Exception::class)
    private fun updateAccount(keyPair: KeyPair, bip32KeyPair: KeyPair) {
        val pair = grpc.updateAccount(keyPair, bip32KeyPair, accountID)
        val txn = pair.first
        val response = pair.second
        log(response.toString())
        val status = response.nodeTransactionPrecheckCode
        when (status) {
            ResponseCodeEnum.OK -> {
                getReceipt(accountID, APIRequestBuilder.getTxnBody(txn).transactionID).let {
                    val s = it.transactionGetReceipt.receipt.status
                    if (s != ResponseCodeEnum.SUCCESS)
                        throw Exception(Singleton.getErrorMessage(s))
                    else
                        migrationStatus = MigrateStatus.Failed_Consensus
                }
            }
            else -> {
                migrationStatus = MigrateStatus.Failed_Update
                throw Exception(Singleton.getErrorMessage(status))
            }
        }

    }

    enum class MigrateStatus {
        Initial, Failed_Update, Failed_Network, Failed_Verfiy_Key_Update, Failed_Consensus, Success
    }
}