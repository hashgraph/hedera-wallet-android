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
import hedera.hgc.hgcwallet.crypto.KeyPair
import hedera.hgc.hgcwallet.hapi.*
import hedera.hgc.hgcwallet.modals.HGCAccountID
import io.grpc.StatusRuntimeException

class MigrateToBip32Task(private val oldKey: KeyPair, private val newKey: KeyPair, private val accountID: HGCAccountID, private val verifyOnly: Boolean = false) : APIBaseTask() {


    var migrationStatus = MigrateStatus.Failed_Update

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
            if (e is StatusRuntimeException)
                migrationStatus = MigrateStatus.Failed_Network
        }
    }

    @Throws(Exception::class)
    private fun getAccountInfo(keyPair: KeyPair): Boolean {
        val txnBuilder = TransactionBuilder(keyPair, accountID)
        val cost = getAccountInfoCost(txnBuilder)
        val accountInfoParam = GetAccountInfoParam(accountID, cost)
        val (query, response) = grpc.perform(accountInfoParam, txnBuilder)
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
        val txnBuilder = TransactionBuilder(keyPair, accountID)
        val updateAccountParam = UpdateAccountParam(bip32KeyPair)
        val (txn, response) = grpc.perform(updateAccountParam, txnBuilder)
        log(response.toString())
        val status = response.nodeTransactionPrecheckCode
        when (status) {
            ResponseCodeEnum.OK -> {
                getReceipt(accountID, txn.getTxnBody().transactionID).let {
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


    @Throws(Exception::class)
    private fun getAccountInfoCost(txnBuilder: TransactionBuilder): Long {
        val accountInfoParam = GetAccountInfoParam(accountID)
        val (query, response) = grpc.perform(accountInfoParam, txnBuilder)
        val status = response.cryptoGetInfo.header.nodeTransactionPrecheckCode
        when (status) {
            ResponseCodeEnum.OK -> return response.cryptoGetInfo.header.cost
            else -> throw Exception(Singleton.getErrorMessage(status))
        }
    }

    enum class MigrateStatus {
        Failed_Update, Failed_Network, Failed_Verfiy_Key_Update, Failed_Consensus, Success
    }
}