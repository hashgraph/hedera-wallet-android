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

package hedera.hgc.hgcwallet.database.transaction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.protobuf.InvalidProtocolBufferException
import com.hederahashgraph.api.proto.java.*
import hedera.hgc.hgcwallet.database.contact.Contact
import hedera.hgc.hgcwallet.hapi.getTxnBody
import hedera.hgc.hgcwallet.modals.HGCAccountID
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue


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
                var maxAmount: Long = 0
                for (accountAmount in transferList.accountAmountsList) {
                    if (maxAmount <= accountAmount.amount.absoluteValue) {
                        maxAmount = accountAmount.amount.absoluteValue
                        if (accountAmount.amount > 0) {
                            toAccountId = HGCAccountID(accountAmount.accountID).stringRepresentation()
                            toAccId = toAccountId
                            amount = accountAmount.amount
                        } else {
                            fromAccId = HGCAccountID(accountAmount.accountID).stringRepresentation()
                            amount = accountAmount.amount.absoluteValue

                        }
                    }
                }

                if (fromAccId == null || toAccId == null) {
                    amount = transferList.accountAmountsList.mapNotNull { if (it.amount < 0) it.amount.absoluteValue else null }.sum()
                    fee = 0
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
            val transactionBody = transaction.getTxnBody()
            fee = transactionBody.transactionFee
            notes = transactionBody.memo
            val cryptoTransfer = transactionBody.cryptoTransfer
            if (transactionBody.cryptoTransfer.hasTransfers()) {
                val transferList = cryptoTransfer.transfers
                for (accountAmount in transferList.accountAmountsList) {
                    if (accountAmount.amount > 0) {
                        toAccountId = HGCAccountID(accountAmount.accountID).stringRepresentation()
                        toAccId = toAccountId
                        amount = accountAmount.amount
                    } else {
                        fromAccId = HGCAccountID(accountAmount.accountID).stringRepresentation()

                    }
                }
            } else if (transactionBody.cryptoCreateAccount.hasKey()) {
                fromAccId = HGCAccountID(transactionBody.transactionID.accountID).stringRepresentation()
                amount = transactionBody.cryptoCreateAccount.initialBalance
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
            if (receiptRes.accountID != null && receiptRes.accountID.accountNum > 0) {
                toAccountId = HGCAccountID(receiptRes.accountID).stringRepresentation()

            }
        }
    }
}
