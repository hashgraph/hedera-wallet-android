package hedera.hgc.hgcwallet.hapi

import com.hederahashgraph.api.proto.java.TransactionID
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc
import com.hederahashgraph.service.proto.java.FileServiceGrpc
import com.hederahashgraph.service.proto.java.SmartContractServiceGrpc
import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.crypto.KeyPair
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.database.node.Node
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.PublicKeyAddress
import io.grpc.*
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GRPCWrapper(nodes: List<Node>) {
    private var currentNodeIndex = 0
    var timeout: Long = 15
    private val nodes: MutableList<Node>

    init {
        this.nodes = nodes.toMutableList()
        currentNodeIndex = randomIndex()
    }

    val node: Node
        get() = nodes[currentNodeIndex]

    private fun randomIndex(): Int {
        return if (nodes.isNotEmpty()) {
            Random.nextInt(0, nodes.size)
        } else
            0
    }

    private var maxRetryCount = max(min((nodes.size * 2) / 3, 10), 1)

    fun channel(): ManagedChannel = ManagedChannelBuilder.forAddress(node.host, node.port).usePlaintext().build()

    fun cryptoStub(): CryptoServiceGrpc.CryptoServiceBlockingStub = CryptoServiceGrpc.newBlockingStub(channel()).withDeadlineAfter(timeout, TimeUnit.SECONDS)

    fun fileStub(): FileServiceGrpc.FileServiceBlockingStub = FileServiceGrpc.newBlockingStub(channel())

    fun tokenStub() = SmartContractServiceGrpc.newBlockingStub(channel())

    fun cryptoTransfer(fromAccount: Account, toAccount: HGCAccountID, amount: Long, notes: String, fee: Long) = perform {
        val transaction = APIRequestBuilder.requestForTransfer(fromAccount, toAccount, amount, notes, fee, node.accountID(), false)
        log(transaction.toString())
        return@perform Pair(transaction, cryptoStub().cryptoTransfer(transaction))
    }

    fun cryptoCreateAccount(fromAccount: Account, keyAddress: PublicKeyAddress, amount: Long, fee: Long) = perform {
        val transaction = TransactionBuilder(Singleton.keyForAccount(fromAccount), fromAccount.accountID()!!, node.accountID()).run {
            createAccountTransaction(keyAddress,amount)
        }
        log(transaction.toString())
        return@perform Pair(transaction, cryptoStub().cryptoTransfer(transaction))
    }

    fun getTransactionReceipt(account: HGCAccountID, txnID: TransactionID) = perform {
        val receiptReq = TransactionBuilder(null, account, node.accountID()).run {
            requestForGetTxnReceipt(txnID)
        }
        log(receiptReq.toString())
        return@perform cryptoStub().getTransactionReceipts(receiptReq)
    }

    fun getAccountRecords(payer: Account, accountID: HGCAccountID) = perform {
        val query = APIRequestBuilder.requestForGetAccountRecord(payer, accountID, node.accountID())
        log(query.toString())
        return@perform cryptoStub().getAccountRecords(query)
    }

    fun cryptoGetBalance(payer: KeyPair, payerAccouintID:HGCAccountID, accountID: HGCAccountID) = perform {
        val query = TransactionBuilder(payer, payerAccouintID, node.accountID()).run {
            requestForGetBalance(accountID)
        }
        log(query.toString())
        return@perform cryptoStub().cryptoGetBalance(query)
    }

    fun updateAccount(keyPair: KeyPair, bip32KeyPair: KeyPair, accountID: HGCAccountID) = perform {
        val txn = TransactionBuilder(keyPair, accountID, node.accountID()).run {
            updateAccountTransaction(bip32KeyPair)
        }
        log(txn.toString())
        return@perform Pair(txn, cryptoStub().updateAccount(txn))

    }


    fun getFileContent(keyPair: KeyPair, accountID: HGCAccountID, fileNum: Long) = perform {
        val txn = TransactionBuilder(keyPair, accountID, node.accountID()).run {
            getFileContentQuery(fileNum)
        }
        log(txn.toString())
        return@perform fileStub().getFileContent(txn)

    }

    fun getAccountInfo(keyPair: KeyPair, accountID: HGCAccountID) = perform {
        val query = TransactionBuilder(keyPair, accountID, node.accountID()).run {
            getAccountInfoQuery(accountID)
        }
        log(query.toString())
        return@perform cryptoStub().getAccountInfo(query)
    }


    private fun <T> perform(block: () -> T): T {
        var error: Exception? = null
        for (i in 1..maxRetryCount) {
            try {
                log(" >>> ${node.address()}")
                return block()
            } catch (e: Exception) {
                error = e
                var shouldRetry = false
                when (e) {
                    is StatusRuntimeException -> {
                        when (e.status.code) {
                            Status.Code.UNAVAILABLE -> shouldRetry = true
                            else -> Unit
                        }
                    }

                    is StatusException -> {
                        when (e.status.code) {
                            Status.Code.UNAVAILABLE -> shouldRetry = true
                            else -> Unit
                        }
                    }
                    else -> Unit
                }

                if (shouldRetry && nodes.size > 1) {
                    nodes.removeAt(currentNodeIndex)
                    currentNodeIndex = randomIndex()
                } else
                    break
            }
        }
        throw error!!
    }

    fun log(message: String) {
        if (Config.isLoggingEnabled) {
            val LOG = LoggerFactory.getLogger(this.javaClass)
            LOG.debug(message)
            Singleton.apiLogs.append(message)
        }
    }

}