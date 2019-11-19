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

package hedera.hgc.hgcwallet.hapi

import com.hederahashgraph.api.proto.java.*
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


    fun perform(param: QueryParams, txnBuilder: TransactionBuilder): Pair<Query, Response> {

        return perform {
            txnBuilder.node = node.accountID()
            val query = param.getQuery(txnBuilder)
            log(query.toFormattedString())

            val res = param.perform(query, this)
            log(res.toString())
            Pair(query, res)
        }
    }

    fun perform(param: TransactionParams, txnBuilder: TransactionBuilder): Pair<Transaction, TransactionResponse> {
        return perform {
            txnBuilder.node = node.accountID()
            val transaction = param.getTransaction(txnBuilder)
            log(transaction.toFormattedString())
            val res = param.perform(transaction, this)
            log(res.toString())
            Pair(transaction, res)
        }
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

interface TransactionParams {
    fun getTransaction(txnBuilder: TransactionBuilder): Transaction
    fun perform(transaction: Transaction, grpc: GRPCWrapper): TransactionResponse
}

interface QueryParams {
    fun getQuery(txnBuilder: TransactionBuilder): Query
    fun perform(query: Query, grpc: GRPCWrapper): Response
}
