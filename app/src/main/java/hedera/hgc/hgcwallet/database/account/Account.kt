package hedera.hgc.hgcwallet.database.account


import androidx.room.*

import java.util.Date

import hedera.hgc.hgcwallet.database.contact.Contact
import hedera.hgc.hgcwallet.database.wallet.Wallet
import hedera.hgc.hgcwallet.modals.HGCKeyType
import hedera.hgc.hgcwallet.modals.HGCAccountID

import androidx.room.ForeignKey.CASCADE
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.hapi.TransactionBuilder
import hedera.hgc.hgcwallet.modals.PublicKeyAddress

@Entity(foreignKeys = [ForeignKey(entity = Wallet::class, parentColumns = ["walletId"], childColumns = ["walletId"], onDelete = CASCADE)])
data class Account(
        var walletId: Long = 0,
        var keyType: String = "",
        @ColumnInfo(name = "keySequenceIndex") var accountIndex: Long = 0,
        @PrimaryKey(autoGenerate = true)
        var UID: Long = 0,
        var name: String = "",
        var balance: Long = 0,
        var lastBalanceCheck: Date? = null,
        var realmNum: Long = 0,
        var shardNum: Long = 0,
        var accountNum: Long = 0,
        var isArchived: Boolean = false,
        var isHidden: Boolean = false,
        var accountType: String = AccountType.AUTO.value,
        var creationDate: Date = Date()
) {

    fun getContact(): Contact? {
        val accountID = accountID()
        return if (accountID != null) {
            Contact(accountID.stringRepresentation(), name, true)
        } else null
    }

    private fun setAccountID(realmId: Long, shardId: Long, accountId: Long) {
        this.realmNum = realmId
        this.shardNum = shardId
        this.accountNum = accountId
    }

    fun setAccountID(accountID: HGCAccountID) {
        setAccountID(accountID.realmNum, accountID.shardNum, accountID.accountNum)
    }

    fun accountID(): HGCAccountID? {
        return if (realmNum != 0L || shardNum != 0L || accountNum != 0L) {
            HGCAccountID(realmNum, shardNum, accountNum)
        } else null
    }

    fun getHGCKeyType(): HGCKeyType {
        return Wallet.keyTypeFrom(keyType)
    }

    fun getHGCAccountType(): AccountType {
        return accountTypeFrom(accountType)
    }

    fun getPublicKeyAddres(): PublicKeyAddress? {
        return PublicKeyAddress.from(Singleton.publicKeyString(this))
    }

    fun getTransactionBuilder(): TransactionBuilder {
        return TransactionBuilder(Singleton.keyForAccount(this), accountID()!!)
    }

    companion object {
        fun createAccount(walletId: Long, keyType: String, accountIndex: Long, name: String): Account {
            return Account(walletId, keyType, accountIndex, 0, name)
        }

        fun createExternalAccount(walletId: Long, name: String, accountID: HGCAccountID): Account {
            return Account(walletId, "", -1, 0, name).apply {
                accountType = AccountType.EXTERNAL.value
                setAccountID(accountID)
            }

        }

        fun accountTypeFrom(type: String): AccountType {
            return when (type) {
                AccountType.AUTO.value -> AccountType.AUTO
                AccountType.EXTERNAL.value -> AccountType.EXTERNAL
                else -> AccountType.AUTO
            }
        }
    }
}

enum class AccountType(val value: String) {
    AUTO("auto"),
    EXTERNAL("external")
}
