package hedera.hgc.hgcwallet.hapi.tasks

import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.hapi.APIBaseTask

class UpdateTransactionsTask : APIBaseTask() {

    override fun main() {
        super.main()
        val accounts = DBHelper.getAllAccounts()
        if (accounts.isNotEmpty()) {
            val payer = accounts[0]
            if (payer.accountID() == null) {
                return
            }
            for (account in accounts) {
                if (account.accountID() != null) {

                    try {
                        val response = grpc.getAccountRecords(payer, account.accountID()!!)
                        log(response.toString())
                        val precheck = response.cryptoGetAccountRecords.header.nodeTransactionPrecheckCode
                        when (precheck) {
                            ResponseCodeEnum.OK -> {
                                val records = response.cryptoGetAccountRecords.recordsList
                                if (records != null) {
                                    for (record in records) {
                                        DBHelper.createTransaction(record, account.accountID())
                                    }
                                }
                            }
                            ResponseCodeEnum.INVALID_ACCOUNT_ID -> error = "Invalid account " + account.accountID()!!.stringRepresentation()

                            else -> error = Singleton.getErrorMessage(precheck)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (error != null)
                            error = getMessage(e)
                    }

                }
            }
        }
    }

/*    @Throws(Exception::class)
    private fun getCostOfGetAccountRecords(payer: Account, accountID: HGCAccountID?): Long {
        val query = APIRequestBuilder.requestForGetAccountRecordCost(payer, accountID!!, node!!.accountID())
        log(query.toString())
        val response = cryptoStub().getAccountRecords(query)
        log(response.toString())
        val precheck = response.cryptogetAccountBalance.header.nodeTransactionPrecheckCode
        when (precheck) {
            ResponseCodeEnum.OK -> return response.cryptoGetAccountRecords.header.cost

            ResponseCodeEnum.INVALID_ACCOUNT_ID -> {
                error = "Invalid account " + accountID.stringRepresentation()
                throw Exception()
            }

            else -> {
                error = Singleton.getErrorMessage(precheck)
                throw Exception()
            }
        }
    }*/
}
