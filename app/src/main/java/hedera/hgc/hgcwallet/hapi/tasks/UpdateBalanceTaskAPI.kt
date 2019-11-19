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
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.hapi.APIBaseTask
import hedera.hgc.hgcwallet.hapi.GetBalanceParam
import hedera.hgc.hgcwallet.hapi.TransactionBuilder
import hedera.hgc.hgcwallet.modals.HGCAccountID
import java.util.*


class UpdateBalanceTaskAPI(val accounts: List<Account>) : APIBaseTask() {

    override fun main() {
        super.main()
        if (accounts.isNotEmpty()) {

            val errorList = mutableListOf<String>()
            val payerAccount = HGCAccountID.fromString("0.0.0")!!

            accounts.forEach { account ->
                account.accountID()?.let { accID ->
                    var err: String? = null
                    try {
                        val txnBuilder = TransactionBuilder(null, payerAccount)
                        val getBalanceParam = GetBalanceParam(accID)
                        val (query, response) = grpc.perform(getBalanceParam, txnBuilder)
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
                                err = "${Singleton.getErrorMessage(precheck)} ${accID.stringRepresentation()}"
                            }

                            else -> err = "${Singleton.getErrorMessage(precheck)} ${accID.stringRepresentation()}"
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()
                        err = getMessage(e)
                    }
                    err?.let { errorList.add(err) }
                }
            }

            if (errorList.isNotEmpty())
                error = errorList.joinToString("\n")
        }
    }
}