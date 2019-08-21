package hedera.hgc.hgcwallet.database.node

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.hederahashgraph.api.proto.java.AccountID
import com.hederahashgraph.api.proto.java.NodeAddress
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.modals.HGCAccountID
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

    fun address() = "$host:$port"

    companion object {
        fun placeholderNode(): Node {
            return Node()
        }

        fun getNode(host: String, port: Int, accountNum: Long, shardNum: Long, realmNum: Long) = Node().apply {
            this.host = host
            this.port = port
            this.accountNum = accountNum
            this.shardNum = shardNum
            this.realmNum = realmNum
        }

        fun from(address: NodeAddress): Node? {
            return address.ipAddress.toString(Charsets.UTF_8)?.let { host ->
                address.memo.toString(Charsets.UTF_8)?.let { memo ->
                    HGCAccountID.fromString(memo)?.let { hgcAccountID ->
                        getNode(host, address.portno, hgcAccountID.accountNum, hgcAccountID.shardNum, hgcAccountID.realmNum)
                    }
                }
            }
        }
    }
}