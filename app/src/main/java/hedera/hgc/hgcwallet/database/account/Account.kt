package hedera.hgc.hgcwallet.database.account

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

import java.util.Date

import hedera.hgc.hgcwallet.database.contact.Contact
import hedera.hgc.hgcwallet.database.wallet.Wallet
import hedera.hgc.hgcwallet.modals.HGCKeyType
import hedera.hgc.hgcwallet.modals.HGCAccountID

import android.arch.persistence.room.ForeignKey.CASCADE
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.modals.PublicKeyAddress

@Entity(foreignKeys = [ForeignKey(entity = Wallet::class, parentColumns = ["walletId"], childColumns = ["walletId"], onDelete = CASCADE)], indices = [Index("accountIndex"), Index("walletId")])
data class Account(
        var walletId: Long = 0,
        var keyType: String = "",
        @PrimaryKey
        var accountIndex: Long = 0,
        var name: String = "",
        var balance: Long = 0,
        var lastBalanceCheck: Date? = null,
        var realmNum: Long = 0,
        var shardNum: Long = 0,
        var accountNum: Long = 0,
        var isArchived: Boolean = false,
        var isHidden: Boolean = false
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

    fun getPublicKeyAddres(): PublicKeyAddress? {
        return PublicKeyAddress.from(Singleton.publicKeyString(this))
    }

    companion object {
        fun createAccount(walletId: Long, keyType: String, accountIndex: Long, name: String): Account {
            return Account(walletId, keyType, accountIndex, name)
        }
    }
}
