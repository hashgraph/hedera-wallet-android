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

import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException
import com.hederahashgraph.api.proto.java.*
import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.crypto.KeyPair
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.PublicKeyAddress
import java.util.*

class TransactionBuilder(payerCredentials: KeyPair?, val payerAccount: HGCAccountID, var node: HGCAccountID = HGCAccountID(0, 0, 0)) {

    private val credentials: MutableList<KeyPair>
    internal val txnFee = Singleton.getDefaultFee()

    init {
        credentials = mutableListOf<KeyPair>()
        payerCredentials?.let { credentials.add(it) }
    }

    internal fun addCredentials(keyPair: KeyPair) {
        credentials.add(keyPair)
    }

    fun createTxnBody(memo: String?, fee: Long, genRecord: Boolean = false): TransactionBody.Builder {

        val transactionID = TransactionID.newBuilder().run {
            accountID = payerAccount.protoAccountID()
            transactionValidStart = createTimestamp(Date())
            build()
        }

        return TransactionBody.newBuilder().apply {
            this.transactionID = transactionID
            transactionFee = fee
            generateRecord = genRecord
            this.memo = memo
            transactionValidDuration = createDuration(120000)
            nodeAccountID = node.protoAccountID()
        }
    }

    fun createTransferBody(from: HGCAccountID, to: HGCAccountID, amount: Long): CryptoTransferTransactionBody.Builder {
        val accountAmount1 = AccountAmount.newBuilder().run {
            this.amount = amount * -1
            accountID = from.protoAccountID()
            build()
        }
        val accountAmount2 = AccountAmount.newBuilder().run {
            this.amount = amount
            accountID = to.protoAccountID()
            build()
        }

        val list = TransferList.newBuilder().run {
            addAccountAmounts(accountAmount1)
            addAccountAmounts(accountAmount2)
            build()
        }

        return CryptoTransferTransactionBody.newBuilder().apply { transfers = list }

    }

    private fun createTimestamp(date: Date): Timestamp {
        val a: Long = 1000
        var millis = date.time
        /*  it look like there is some offset added sometime during the day time or so,
            that causes start time validation failure at serivce side
            delaying by 15 seconds fixed it
        */
        millis -= (15 * 1000).toLong()
        val seconds = millis / a
        val n = ((millis - seconds * a) * 1000000).toInt()
        return Timestamp.newBuilder().setSeconds(seconds).setNanos(n).build()
    }

    internal fun createDuration(millis: Long): Duration {
        val a: Long = 1000
        val seconds = millis / a
        val n = 0
        return Duration.newBuilder().setSeconds(seconds).build()
    }

    fun createSignedTransaction(body: TransactionBody, forThirdParty: Boolean = false): Transaction {

        return Transaction.newBuilder().run {
            if (Config.useBetaAPIs) {
                bodyBytes = body.toByteString()
                sigMap = SignatureMap.newBuilder().run {
                    credentials.forEach {
                        val sig = createSignaturePair(body, forThirdParty, it)
                        addSigPair(sig)
                    }
                    build()
                }
            } else {
                this.body = body
                sigs = SignatureList.newBuilder().run {
                    credentials.forEach {
                        val signature = createSignature(body, it)
                        addSigs(signature)
                        addSigs(signature)
                    }

                    build()
                }
            }
            build()
        }
    }

    private fun createSignaturePair(body: TransactionBody, useFullKeyPrefix: Boolean = false, keyPair: KeyPair): SignaturePair {
        return SignaturePair.newBuilder().run {
            body.toByteArray()?.let {
                val s = keyPair.signMessage(it)
                ed25519 = ByteString.copyFrom(s)
            }

            pubKeyPrefix = if (useFullKeyPrefix) ByteString.copyFrom(keyPair.publicKey) else ByteString.copyFrom(keyPair.publicKey, 0, 4)

            build()
        }
    }

    private fun createSignature(body: TransactionBody, keyPair: KeyPair): Signature {
        return Signature.newBuilder().run {
            body.toByteArray()?.let {
                val s = keyPair.signMessage(it)
                ed25519 = ByteString.copyFrom(s)
            }
            build()
        }
    }

    fun createQueryHeader(memo: String, queryFee: Long, rType: ResponseType = ResponseType.ANSWER_ONLY): QueryHeader {
        val body = createTxnBody(memo, txnFee).run {
            cryptoTransfer = createTransferBody(payerAccount, node, queryFee).build()
            build()
        }

        return QueryHeader.newBuilder().run {
            payment = createSignedTransaction(body)
            responseType = rType
            build()
        }
    }
}

data class GetTransactionReceiptParam(private val txnId: TransactionID) : QueryParams {

    override fun getQuery(txnBuilder: TransactionBuilder): Query {
        return Query.newBuilder().run {
            transactionGetReceipt = TransactionGetReceiptQuery.newBuilder().run {
                this.header = txnBuilder.createQueryHeader("for receipt", txnBuilder.txnFee)
                transactionID = txnId
                build()
            }
            build()
        }
    }

    override fun perform(query: Query, grpc: GRPCWrapper): Response {
        return grpc.cryptoStub().getTransactionReceipts(query)
    }
}

data class GetBalanceParam(private val hgcAccountID: HGCAccountID) : QueryParams {

    override fun getQuery(txnBuilder: TransactionBuilder): Query {
        val getAccountBalanceQuery = CryptoGetAccountBalanceQuery.newBuilder().run {
            header = txnBuilder.createQueryHeader("for balance check", txnBuilder.txnFee)
            accountID = hgcAccountID.protoAccountID()
            build()
        }

        return Query.newBuilder().run {
            cryptogetAccountBalance = getAccountBalanceQuery
            build()
        }
    }

    override fun perform(query: Query, grpc: GRPCWrapper): Response {
        return grpc.cryptoStub().cryptoGetBalance(query)
    }
}

data class UpdateAccountParam(private val keyPair: KeyPair) : TransactionParams {

    override fun getTransaction(txnBuilder: TransactionBuilder): Transaction {
        val newKey = Key.newBuilder().run {
            ed25519 = ByteString.copyFrom(keyPair.publicKey)
            build()
        }
        val body = CryptoUpdateTransactionBody.newBuilder().run {
            accountIDToUpdate = txnBuilder.payerAccount.protoAccountID()
            key = newKey
            build()
        }
        val txnBody = txnBuilder.createTxnBody("for update account", txnBuilder.txnFee, true).run {
            cryptoUpdateAccount = body
            build()
        }

        txnBuilder.addCredentials(keyPair)
        return txnBuilder.createSignedTransaction(txnBody)
    }

    override fun perform(transaction: Transaction, grpc: GRPCWrapper): TransactionResponse {
        return grpc.cryptoStub().updateAccount(transaction)
    }
}

data class GetFileContentParam(private val fileNumber: Long, private val fee: Long? = null) : QueryParams {

    override fun getQuery(txnBuilder: TransactionBuilder): Query {
        val fileID = FileID.newBuilder().run {
            shardNum = 0
            realmNum = 0
            fileNum = fileNumber
            build()
        }

        val fileQuery = FileGetContentsQuery.newBuilder().run {
            header = if (fee == null)
                txnBuilder.createQueryHeader("for get file content fee", txnBuilder.txnFee, ResponseType.COST_ANSWER)
            else
                txnBuilder.createQueryHeader("for get file content", fee)


            this.fileID = fileID
            build()
        }

        return Query.newBuilder().run {
            fileGetContents = fileQuery
            build()
        }
    }

    override fun perform(query: Query, grpc: GRPCWrapper): Response {
        return grpc.fileStub().getFileContent(query)
    }
}

data class CreateAccountParams(
        private val publicKeyAddress: PublicKeyAddress,
        private val amount: Long,
        private val fee: Long,
        private val memo: String
) : TransactionParams {

    override fun getTransaction(txnBuilder: TransactionBuilder): Transaction {
        val newKey = Key.newBuilder().run {
            ed25519 = ByteString.copyFrom(publicKeyAddress.getByteArray())
            build()
        }

        val body = CryptoCreateTransactionBody.newBuilder().run {
            initialBalance = amount
            autoRenewPeriod = txnBuilder.createDuration(1000L * 7890000L)
            key = newKey
            build()
        }
        val txnBody = txnBuilder.createTxnBody(memo, fee, true).run {
            cryptoCreateAccount = body
            build()
        }

        return txnBuilder.createSignedTransaction(txnBody)
    }

    override fun perform(transaction: Transaction, grpc: GRPCWrapper): TransactionResponse {
        return grpc.cryptoStub().createAccount(transaction)
    }
}

data class GetAccountInfoParam(private val account: HGCAccountID, private val fee: Long? = null) : QueryParams {

    override fun getQuery(txnBuilder: TransactionBuilder): Query {
        val getAccountInfouery = CryptoGetInfoQuery.newBuilder().run {

            header = if (fee == null)
                txnBuilder.createQueryHeader("for get account info cost", txnBuilder.txnFee, ResponseType.COST_ANSWER)
            else
                txnBuilder.createQueryHeader("for get account info", fee)
            accountID = account.protoAccountID()
            build()
        }

        return Query.newBuilder().run {
            cryptoGetInfo = getAccountInfouery
            build()
        }
    }

    override fun perform(query: Query, grpc: GRPCWrapper): Response {
        return grpc.cryptoStub().getAccountInfo(query)
    }
}

data class GetAccountRecordParam(private val hgcAccountID: HGCAccountID, private val fee: Long? = null) : QueryParams {

    override fun getQuery(txnBuilder: TransactionBuilder): Query {
        val header = if (fee == null)
            txnBuilder.createQueryHeader("for account record cost", txnBuilder.txnFee, ResponseType.COST_ANSWER)
        else
            txnBuilder.createQueryHeader("for account record", fee)

        return Query.newBuilder().run {
            setCryptoGetAccountRecords(CryptoGetAccountRecordsQuery.newBuilder().apply {
                this.header = header
                this.accountID = hgcAccountID.protoAccountID()
            })
            build()
        }
    }

    override fun perform(query: Query, grpc: GRPCWrapper): Response {
        return grpc.cryptoStub().getAccountRecords(query)
    }
}

data class TransferParam(
        val toAccount: HGCAccountID,
        private val amount: Long,
        private val notes: String?,
        private val fee: Long,
        private val forThirdParty: Boolean,
        val toAccountName: String? // This is for reference only, does not get involved in transaction
) : TransactionParams {

    override fun getTransaction(txnBuilder: TransactionBuilder): Transaction {
        return txnBuilder.run {
            val body = createTxnBody(notes, fee, true).run {
                setCryptoTransfer(createTransferBody(txnBuilder.payerAccount, toAccount, amount))
                build()
            }
            createSignedTransaction(body, forThirdParty)
        }
    }

    override fun perform(transaction: Transaction, grpc: GRPCWrapper): TransactionResponse {
        return grpc.cryptoStub().cryptoTransfer(transaction)
    }
}


fun Transaction.getTxnBody(): TransactionBody {
    val bodyBytes = bodyBytes
    if (bodyBytes != null && bodyBytes.size() > 0) {
        try {
            return TransactionBody.parseFrom(bodyBytes)
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }

    }
    return body
}

fun Transaction.toFormattedString(): String {

    return this.toString() + "\n transactionBody: ${this.getTxnBody()}"
}

fun Query.toFormattedString(): String {

    val payment = when {
        cryptoGetAccountRecords.hasHeader() -> cryptoGetAccountRecords.header.payment
        fileGetContents.hasHeader() -> fileGetContents.header.payment
        cryptoGetInfo.hasHeader() -> cryptoGetInfo.header.payment
        else -> cryptogetAccountBalance.header.payment
    }

    return this.toString() + "\n paymentBody: ${payment.getTxnBody()}"
}
