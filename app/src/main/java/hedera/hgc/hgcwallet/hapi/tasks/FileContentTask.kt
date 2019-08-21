package hedera.hgc.hgcwallet.hapi.tasks

import com.google.protobuf.ByteString
import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.hapi.APIBaseTask
import hedera.hgc.hgcwallet.hapi.APIRequestBuilder

class FileContentTask(private var payerAccount: Account, private var fileNum: Long) : APIBaseTask() {

     var fileContent: ByteString? = null

    override fun main() {
        super.main()
        getFileContent()
    }

    private fun getFileContent() {
        try {
            val response = grpc.getFileContent(Singleton.keyForAccount(payerAccount), payerAccount.accountID()!!, fileNum)
            log(response.toString())
            val code = response.fileGetContents.header.nodeTransactionPrecheckCode
            when (code) {
                ResponseCodeEnum.OK -> fileContent = response.fileGetContents.fileContents.contents
                else -> {
                    error = Singleton.getErrorMessage(code)
                    log(error)
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
            error = getMessage(e)
        }
    }
}