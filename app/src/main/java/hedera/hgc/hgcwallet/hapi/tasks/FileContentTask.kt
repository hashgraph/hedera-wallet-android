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