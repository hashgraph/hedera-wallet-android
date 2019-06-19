package opencrowd.hgc.hgcwallet.database.transaction

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.google.protobuf.InvalidProtocolBufferException
import com.hederahashgraph.api.proto.java.*
import opencrowd.hgc.hgcwallet.hapi.APIRequestBuilder
import opencrowd.hgc.hgcwallet.database.contact.Contact
import opencrowd.hgc.hgcwallet.modals.HGCAccountID
import java.util.*


@Entity
data class TxnRecord(
        @PrimaryKey
        var txnId: TransactionID,

        var fromAccId: String? = null,
        var toAccId: String? = null,
        var createdDate: Date? = null,

        @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
        var txn: ByteArray? = null,

        @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
        var receipt: ByteArray? = null,

        @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
        var record: ByteArray? = null,

        @Ignore
        var fromAccount: Contact? = null,

        @Ignore
        var toAccount: Contact? = null,

        @Ignore
        var amount: Long = 0,

        @Ignore
        var fee: Long = 0,

        @Ignore
        var toAccountId: String? = null,

        @Ignore
        var notes: String = "",

        @Ignore
        var isPositive: Boolean = false,

        @Ignore
        var status: Status = Status.UNKNOWN

) {
    constructor(txnId: TransactionID) : this(txnId, null, null, Date(), null, null, null, null, null, 0, 0, null, "", false, Status.UNKNOWN)

    enum class Status {
        UNKNOWN, FAILED, SUCCESS
    }

    fun setTxn(transaction: Transaction) {
        txn = transaction.toByteArray()
    }

    fun setReceipt(receiptResponse: TransactionReceipt) {
        receipt = receiptResponse.toByteArray()
    }

    fun setRecord(recordResponse: Response) {
        receipt = recordResponse.toByteArray()
    }

    fun parseProperties() {
        if (record == null) {
            parseTxn()
            parseReceipt()
        } else {
            parseRecord()
        }

    }

    private fun parseRecord() {
        if (record == null) return
        var transactionRecord: TransactionRecord? = null
        try {
            transactionRecord = TransactionRecord.parseFrom(record)
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }

        if (transactionRecord != null) {
            fee = transactionRecord.transactionFee
            notes = transactionRecord.memo
            val receiptRes = transactionRecord.receipt
            if (receiptRes != null) {
                when (receiptRes.status) {
                    ResponseCodeEnum.SUCCESS -> status = Status.SUCCESS
                    ResponseCodeEnum.UNKNOWN, ResponseCodeEnum.UNRECOGNIZED -> {
                    }
                    else -> status = Status.FAILED
                }
            }
            val transferList = transactionRecord.transferList
            if (transferList != null) {
                for (accountAmount in transferList.accountAmountsList) {
                    if (accountAmount.amount > 0) {
                        toAccountId = HGCAccountID(accountAmount.accountID).stringRepresentation()
                        amount = accountAmount.amount
                    } else {
                        fromAccId = HGCAccountID(accountAmount.accountID).stringRepresentation()
                    }
                }
            }

        }
    }

    private fun parseTxn() {
        if (txn == null) return
        var transaction: Transaction? = null
        try {
            transaction = Transaction.parseFrom(txn)
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }

        if (transaction != null) {
            val transactionBody = APIRequestBuilder.getTxnBody(transaction)
            fee = transactionBody.transactionFee
            notes = transactionBody.memo
            val cryptoTransfer = transactionBody.cryptoTransfer
            if (cryptoTransfer != null) {
                val transferList = cryptoTransfer.transfers
                for (accountAmount in transferList.accountAmountsList) {
                    if (accountAmount.amount > 0) {
                        toAccountId = HGCAccountID(accountAmount.accountID).stringRepresentation()
                        amount = accountAmount.amount
                    } else {
                        fromAccId = HGCAccountID(accountAmount.accountID).stringRepresentation()
                    }
                }
            }
        }
    }

    private fun parseReceipt() {
        if (receipt == null) return
        var receiptRes: TransactionReceipt? = null
        try {
            receiptRes = TransactionReceipt.parseFrom(receipt)
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }

        if (receiptRes != null) {
            when (receiptRes.status) {
                ResponseCodeEnum.SUCCESS -> status = Status.SUCCESS
                ResponseCodeEnum.UNKNOWN, ResponseCodeEnum.UNRECOGNIZED -> {
                }
                else -> status = Status.FAILED
            }
        }
    }
}
