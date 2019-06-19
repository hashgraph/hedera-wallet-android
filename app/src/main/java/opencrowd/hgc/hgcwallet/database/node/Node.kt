package opencrowd.hgc.hgcwallet.database.node

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import opencrowd.hgc.hgcwallet.modals.HGCAccountID
import java.util.*


@Entity
data class Node(
        @PrimaryKey(autoGenerate = true)
        var nodeId: Long = 0,
        var realmNum: Long = 0,
        var shardNum: Long = 0,
        var accountNum: Long = 0,
        var host: String? = "",
        var port: Int = 0,
        var status: String? = "",
        var lastCheckAt: Date? = null,
        var disabled: Boolean = false
) {
    private fun setAccountID(realmId: Long, shardId: Long, accountId: Long) {
        this.realmNum = realmId
        this.shardNum = shardId
        this.accountNum = accountId
    }

    fun setAccountID(accountID: HGCAccountID) {
        setAccountID(accountID.realmNum, accountID.shardNum, accountID.accountNum)
    }

    fun accountID(): HGCAccountID {
        return HGCAccountID(realmNum, shardNum, accountNum)
    }

    companion object {
        fun placeholderNode(): Node {
            return Node()
        }
    }
}