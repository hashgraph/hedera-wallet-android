package hedera.hgc.hgcwallet.hapi

import com.hederahashgraph.api.proto.java.Response
import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import com.hederahashgraph.api.proto.java.TransactionID
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.common.BaseTask
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.modals.HGCAccountID
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory


abstract class APIBaseTask : BaseTask() {
    var LOG = LoggerFactory.getLogger(this.javaClass)
    var grpc = GRPCWrapper(App.instance.addressBook?.getNodes(true)!!)

    override fun main() {

    }

    @Throws(Exception::class)
    fun getReceipt(account: HGCAccountID, txnID: TransactionID, retryCount: Int = 3): Response {
        var response: Response? = null
        for (i in 1..retryCount) {
            try {
                response = grpc.getTransactionReceipt(account, txnID)
                log(response.toString())
                if (response.transactionGetReceipt.receipt.status == ResponseCodeEnum.SUCCESS)
                    break
            } catch (e: Exception) {
                log(e.toString())
                break
            }
        }
        if (response == null)
            throw Exception("unable to fetch receipt")
        else
            return response

    }

    fun getMessage(e: Exception): String {
        if (e is StatusRuntimeException) {
            when (e.status.code) {
                Status.Code.OK -> {
                }
                Status.Code.CANCELLED -> {
                }
                Status.Code.UNKNOWN, Status.Code.UNAVAILABLE, Status.Code.DEADLINE_EXCEEDED -> return "Node is not reachable: " + grpc.node.host!!
                Status.Code.INVALID_ARGUMENT -> {
                }
                Status.Code.NOT_FOUND -> {
                }
                Status.Code.ALREADY_EXISTS -> {
                }
                Status.Code.PERMISSION_DENIED -> {
                }
                Status.Code.RESOURCE_EXHAUSTED -> {
                }
                Status.Code.FAILED_PRECONDITION -> {
                }
                Status.Code.ABORTED -> {
                }
                Status.Code.OUT_OF_RANGE -> {
                }
                Status.Code.UNIMPLEMENTED -> {
                }
                Status.Code.INTERNAL -> {
                }
                Status.Code.DATA_LOSS -> {
                }
                Status.Code.UNAUTHENTICATED -> {
                }
                else -> {
                }
            }
        }
        return e.message ?: ""
    }

    fun log(message: String) {
        if (Config.isLoggingEnabled) {
            LOG.debug(message)
            Singleton.apiLogs.append(message)
        }
    }
}