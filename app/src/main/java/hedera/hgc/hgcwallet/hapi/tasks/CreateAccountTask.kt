package hedera.hgc.hgcwallet.hapi.tasks

import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import com.hederahashgraph.api.proto.java.Transaction
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.hapi.APIBaseTask
import hedera.hgc.hgcwallet.hapi.CreateAccountParams
import hedera.hgc.hgcwallet.hapi.getTxnBody
import hedera.hgc.hgcwallet.modals.HGCAccountID

class CreateAccountTask(private val params: CreateAccountParams, private val fromAccount: Account) : APIBaseTask() {
    var accountId: HGCAccountID? = null
        private set

    override fun main() {
        super.main()
        try {
            val pairResponse = grpc.perform(params, fromAccount.getTransactionBuilder())
            val code = pairResponse.second.nodeTransactionPrecheckCode
            when (code) {
                ResponseCodeEnum.OK -> {
                    accountId = fetchReceipt(pairResponse.first)
                    accountId?.let {
                        DBHelper.createContact(it, "", "", true)
                    }
                }
                else -> error = Singleton.getErrorMessage(code)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error = getMessage(e)
        }
    }


    private fun fetchReceipt(transaction: Transaction): HGCAccountID? {
        var accountId: HGCAccountID? = null
        try {
            val record = DBHelper.createTransaction(transaction, fromAccount.accountID())
            getReceipt(fromAccount.accountID()!!, transaction.getTxnBody().transactionID)?.let { response ->
                if (response.transactionGetReceipt.hasReceipt()) {
                    record.setReceipt(response.transactionGetReceipt.receipt)
                    val code = response.transactionGetReceipt.receipt.status
                    if (code != ResponseCodeEnum.SUCCESS)
                        error = Singleton.getErrorMessage(code)
                    else
                        accountId = HGCAccountID(response.transactionGetReceipt.receipt.accountID)
                }
            }

            DBHelper.updateTransaction(record)

        } catch (e: Exception) {
            e.printStackTrace()
            error = getMessage(e)
        }

        return accountId
    }


}
