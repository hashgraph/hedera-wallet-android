package hedera.hgc.hgcwallet.hapi

import com.google.protobuf.InvalidProtocolBufferException
import com.hederahashgraph.api.proto.java.CryptoGetAccountRecordsQuery
import com.hederahashgraph.api.proto.java.Query
import com.hederahashgraph.api.proto.java.Transaction
import com.hederahashgraph.api.proto.java.TransactionBody
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.modals.HGCAccountID

object APIRequestBuilder {

    fun requestForTransfer(from: Account, toAccount: HGCAccountID, amount: Long, notes: String?, fee: Long, node: HGCAccountID, forThirdParty: Boolean): Transaction {
        return TransactionBuilder(Singleton.keyForAccount(from), from.accountID()!!, node).run {
            val body = createTxnBody(notes, fee, true).run {
                setCryptoTransfer(createTransferBody(from.accountID()!!, toAccount, amount))
                build()
            }
            createSignedTransaction(body, forThirdParty)
        }
    }

    fun requestForGetAccountRecord(payer: Account, accountID: HGCAccountID, node: HGCAccountID): Query {
        val header = TransactionBuilder(Singleton.keyForAccount(payer), payer.accountID()!!, node).run {
            createQueryHeader("for account record ")
        }
        return Query.newBuilder().run {
            setCryptoGetAccountRecords(CryptoGetAccountRecordsQuery.newBuilder().apply {
                this.header = header
                this.accountID = accountID.protoAccountID()
            })
            build()
        }

    }


    fun getTxnBody(transaction: Transaction): TransactionBody {
        val bodyBytes = transaction.bodyBytes
        if (bodyBytes != null && bodyBytes.size() > 0) {
            try {
                return TransactionBody.parseFrom(bodyBytes)
            } catch (e: InvalidProtocolBufferException) {
                e.printStackTrace()
            }

        }
        return transaction.body
    }


}