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

import com.google.protobuf.ByteString
import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.hapi.APIBaseTask
import hedera.hgc.hgcwallet.hapi.GetFileContentParam

class FileContentTask(private var payerAccount: Account, private var fileNum: Long) : APIBaseTask() {

    var fileContent: ByteString? = null

    override fun main() {
        super.main()
        getFileContent()
    }

    private fun getFileContent() {
        try {
            val cost = getFileContentCost(payerAccount)
            val fileContentParam = GetFileContentParam(fileNum, cost)
            val txnBuilder = payerAccount.getTransactionBuilder()
            val (query, response) = grpc.perform(fileContentParam, txnBuilder)
            val code = response.fileGetContents.header.nodeTransactionPrecheckCode
            when (code) {
                ResponseCodeEnum.OK -> fileContent = response.fileGetContents.fileContents.contents
                else -> {
                    Singleton.getErrorMessage(code).let {
                        error = it
                        log(it)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            error = getMessage(e)
        }
    }

    @Throws(Exception::class)
    private fun getFileContentCost(account: Account): Long {

        val fileContentParam = GetFileContentParam(fileNum)
        val txnBuilder = account.getTransactionBuilder()
        val (query, response) = grpc.perform(fileContentParam, txnBuilder)
        val code = response.fileGetContents.header.nodeTransactionPrecheckCode
        when (code) {
            ResponseCodeEnum.OK -> return response.fileGetContents.header.cost
            else -> throw Exception(Singleton.getErrorMessage(code))
        }
    }
}