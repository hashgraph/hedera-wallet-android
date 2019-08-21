package hedera.hgc.hgcwallet.hapi

import com.google.protobuf.ByteString
import com.hederahashgraph.api.proto.java.*
import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.crypto.KeyPair
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.PublicKeyAddress
import java.util.*

class TransactionBuilder(payerCredentials: KeyPair?, val payerAccount: HGCAccountID, val node: HGCAccountID) {

    private val credentials: MutableList<KeyPair>
    private val fee = Singleton.getDefaultFee()

    init {
        credentials = mutableListOf<KeyPair>()
        payerCredentials?.let { credentials.add(it) }
    }

    private fun addCredentials(keyPair: KeyPair) {
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

    private fun createDuration(millis: Long): Duration {
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

    fun createQueryHeader(memo: String, includePayment: Boolean = true, rType: ResponseType = ResponseType.ANSWER_ONLY): QueryHeader {
        val body = createTxnBody(memo, fee).run {
            cryptoTransfer = createTransferBody(payerAccount, node, fee).build()
            build()
        }

        return QueryHeader.newBuilder().run {
            if (includePayment)
                payment = createSignedTransaction(body)
            responseType = rType
            build()
        }
    }

    fun requestForGetTxnReceipt(txnId: TransactionID): Query {
        return Query.newBuilder().run {
            transactionGetReceipt = TransactionGetReceiptQuery.newBuilder().run {
                this.header = createQueryHeader("for receipt", false)
                transactionID = txnId
                build()
            }
            build()
        }
    }

    fun requestForGetBalance(account: HGCAccountID): Query {

        val getAccountBalanceQuery = CryptoGetAccountBalanceQuery.newBuilder().run {
            header = createQueryHeader("for balance check")
            accountID = account.protoAccountID()
            build()
        }

        return Query.newBuilder().run {
            cryptogetAccountBalance = getAccountBalanceQuery
            build()
        }
    }

    fun updateAccountTransaction(keyPair: KeyPair): Transaction {

        val newKey = Key.newBuilder().run {
            ed25519 = ByteString.copyFrom(keyPair.publicKey)
            build()
        }
        val body = CryptoUpdateTransactionBody.newBuilder().run {
            accountIDToUpdate = payerAccount.protoAccountID()
            key = newKey
            build()
        }
        val txnBody = createTxnBody("for update account", fee, true).run {
            cryptoUpdateAccount = body
            build()
        }

        addCredentials(keyPair)
        return createSignedTransaction(txnBody)

    }

    fun getFileContentQuery(fileNum: Long): Query {
        val fileID = FileID.newBuilder().run {
            shardNum = 0
            realmNum = 0
            this.fileNum = fileNum
            build()
        }

        val fileQuery = FileGetContentsQuery.newBuilder().run {
            header = createQueryHeader("for get file content")
            this.fileID = fileID
            build()
        }

        return Query.newBuilder().run {
            fileGetContents = fileQuery
            build()
        }
    }

    fun createAccountTransaction(publicKeyAddress: PublicKeyAddress, amount: Long): Transaction {

        val newKey = Key.newBuilder().run {
            ed25519 = ByteString.copyFrom(publicKeyAddress.getByteArray())
            build()
        }

        val body = CryptoCreateTransactionBody.newBuilder().run {
            initialBalance = amount
            autoRenewPeriod = createDuration(1000 * 60 * 60 * 24 * 30)
            key = newKey
            build()
        }
        val txnBody = createTxnBody("", fee, true).run {
            cryptoCreateAccount = body
            build()
        }

        return createSignedTransaction(txnBody)

    }

    fun getAccountInfoQuery(account: HGCAccountID): Query {
        val getAccountInfouery = CryptoGetInfoQuery.newBuilder().run {
            header = createQueryHeader("for get account info")
            accountID = account.protoAccountID()
            build()
        }

        return Query.newBuilder().run {
            cryptoGetInfo = getAccountInfouery
            build()
        }
    }
}