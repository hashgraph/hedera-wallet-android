/*
 *
 *  Copyright 2019 Hedera Hashgraph LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package hedera.hgc.hgcwallet.hapi.tasks

import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.hapi.APIBaseTask
import hedera.hgc.hgcwallet.hapi.GetAccountRecordParam
import hedera.hgc.hgcwallet.modals.HGCAccountID

class UpdateTransactionsTask : APIBaseTask() {

    override fun main() {
        super.main()
        getHistory(DBHelper.getAllAccounts())
    }


    private fun getHistory(accounts: List<Account>) {
        if (accounts.isNotEmpty()) {
            val payer = accounts.first()
            if (payer.accountID() == null) {
                return
            }
            for (account in accounts) {
                account.accountID()?.let { accID ->
                    try {
                        val fee = getCostOfAccountRecords(payer, accID)
                        val recordParam = GetAccountRecordParam(accID, fee)
                        val txnBuilder = payer.getTransactionBuilder()
                        val (query, response) = grpc.perform(recordParam, txnBuilder)
                        val precheck = response.cryptoGetAccountRecords.header.nodeTransactionPrecheckCode
                        when (precheck) {
                            ResponseCodeEnum.OK -> {
                                response.cryptoGetAccountRecords.recordsList?.let { records ->
                                    records.forEach {
                                        DBHelper.createTransaction(it)
                                        Singleton.setExchangeRate(it.receipt)
                                    }
                                }
                            }
                            ResponseCodeEnum.PAYER_ACCOUNT_NOT_FOUND -> {
                                error = "payerAccountNotFound " + payer.accountID()?.stringRepresentation()
                                throw Exception()
                            }

                            ResponseCodeEnum.INVALID_ACCOUNT_ID -> {
                                error = "invalidAccountID " + accID.stringRepresentation()
                                throw Exception()
                            }

                            else -> {
                                error = Singleton.getErrorMessage(precheck)
                                throw Exception()
                            }
                        }

                    } catch (e: Exception) {
                        if (error == null)
                            error = getMessage(e)

                        e.printStackTrace()
                    }

                }
            }
        }
    }

    @Throws(Exception::class)
    private fun getCostOfAccountRecords(payer: Account, accountID: HGCAccountID): Long {
        val accountRecordParam = GetAccountRecordParam(accountID)
        val txnBuilder = payer.getTransactionBuilder()
        val (query, response) = grpc.perform(accountRecordParam, txnBuilder)

        val status = response.cryptoGetAccountRecords.header.nodeTransactionPrecheckCode
        when (status) {
            ResponseCodeEnum.OK -> return response.cryptoGetAccountRecords.header.cost

            ResponseCodeEnum.PAYER_ACCOUNT_NOT_FOUND -> {
                error = "payerAccountNotFound " + payer.accountID()?.stringRepresentation()
                throw Exception(error)
            }

            ResponseCodeEnum.INVALID_ACCOUNT_ID -> {
                error = "invalidAccountID " + accountID.stringRepresentation()
                throw Exception(error)
            }

            else -> {
                error = Singleton.getErrorMessage(status)
                throw Exception(error)
            }
        }
    }
}
