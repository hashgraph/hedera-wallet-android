package hedera.hgc.hgcwallet.hapi.tasks

import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import com.hederahashgraph.api.proto.java.Transaction
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.hapi.APIBaseTask
import hedera.hgc.hgcwallet.hapi.TransferParam
import hedera.hgc.hgcwallet.hapi.getTxnBody

class TransferTaskAPI(private val param: TransferParam, private val fromAccount: Account) : APIBaseTask() {

    override fun main() {
        super.main()
        try {
            val pairResponse = grpc.perform(param, fromAccount.getTransactionBuilder())
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
            DBHelper.createContact(param.toAccount, param.toAccountName, "", false)
            getReceipt(fromAccount.accountID()!!, transaction.getTxnBody().transactionID).let { response ->
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
