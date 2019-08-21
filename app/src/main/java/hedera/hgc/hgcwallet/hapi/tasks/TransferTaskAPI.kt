package hedera.hgc.hgcwallet.hapi.tasks

import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import com.hederahashgraph.api.proto.java.Transaction
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.hapi.APIBaseTask
import hedera.hgc.hgcwallet.hapi.APIRequestBuilder
import hedera.hgc.hgcwallet.modals.HGCAccountID

class TransferTaskAPI(private var fromAccount: Account, private var toAccount: HGCAccountID, private var notes: String,
                      private var toAccountName: String, private var amount: Long, private var fee: Long) : APIBaseTask() {

    override fun main() {
        super.main()
        try {
            val pairResponse = grpc.cryptoTransfer(fromAccount, toAccount, amount, notes, fee)
            log(pairResponse.second.toString())

            val code = pairResponse.second.nodeTransactionPrecheckCode
            when (code) {
                ResponseCodeEnum.OK -> {
                    fetchReceipt(pairResponse.first)
                }
                else -> error = Singleton.getErrorMessage(code)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            error = getMessage(e)
        }
    }

    private fun fetchReceipt(transaction: Transaction) {
        try {
            val record = DBHelper.createTransaction(transaction, fromAccount.accountID())
            getReceipt(fromAccount.accountID()!!, APIRequestBuilder.getTxnBody(transaction).transactionID)?.let { response ->
                log(response.toString())
                if (response.transactionGetReceipt.hasReceipt()) {
                    record.setReceipt(response.transactionGetReceipt.receipt)
                    val code = response.transactionGetReceipt.receipt.status
                    if (code != ResponseCodeEnum.SUCCESS)
                        error = Singleton.getErrorMessage(code)
                }
            }

            DBHelper.updateTransaction(record)


        } catch (e: Exception) {
            e.printStackTrace()
            error = getMessage(e)
        }
    }

    private fun sleep() {
        try {
            Thread.sleep(4000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }
}
