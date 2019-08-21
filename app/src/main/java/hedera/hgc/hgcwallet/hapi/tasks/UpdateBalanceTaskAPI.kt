package hedera.hgc.hgcwallet.hapi.tasks

import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.hapi.APIBaseTask
import java.util.*


class UpdateBalanceTaskAPI : APIBaseTask() {

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
                        val response = grpc.cryptoGetBalance(Singleton.keyForAccount(payer), account.accountID()!!, account.accountID()!!)
                        log(response.toString())
                        val precheck = response.cryptogetAccountBalance.header.nodeTransactionPrecheckCode
                        when (precheck) {
                            ResponseCodeEnum.OK -> {
                                val balance = response.cryptogetAccountBalance.balance
                                account.balance = balance
                                account.lastBalanceCheck = Date()
                                DBHelper.saveAccount(account)
                            }
                            ResponseCodeEnum.INVALID_ACCOUNT_ID -> {
                                Singleton.clearAccountData(account)
                                error = "Invalid account " + account.accountID()!!.stringRepresentation()
                            }

                            else -> error = Singleton.getErrorMessage(precheck)
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()
                        error = getMessage(e)
                    }

                }
            }
        }
    }
}