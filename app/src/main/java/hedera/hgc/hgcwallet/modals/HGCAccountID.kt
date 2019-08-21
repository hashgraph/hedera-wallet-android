package hedera.hgc.hgcwallet.modals

import com.hederahashgraph.api.proto.java.AccountID

class HGCAccountID(val realmNum: Long, val shardNum: Long, val accountNum: Long) {

    constructor(accountID: AccountID) : this(accountID.realmNum, accountID.shardNum, accountID.accountNum)

    fun protoAccountID(): AccountID {
        return AccountID.newBuilder()
                .setAccountNum(accountNum)
                .setShardNum(shardNum)
                .setRealmNum(realmNum).build()
    }


    fun stringRepresentation(): String {
        return "$realmNum.$shardNum.$accountNum"
    }

    companion object {
        fun fromString(str: String?): HGCAccountID? {
            var accountID: HGCAccountID? = null
            str?.let {
                val items = it.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (items.size == 3) {
                    try {
                        val firstNum = java.lang.Long.parseLong(items[0])
                        val secondNum = java.lang.Long.parseLong(items[1])
                        val thirdNum = java.lang.Long.parseLong(items[2])
                        accountID = HGCAccountID(firstNum, secondNum, thirdNum)
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }
            }

            return accountID
        }
    }


    override fun equals(obj: Any?): Boolean {
        return (obj as? HGCAccountID)?.let {
            accountNum == it.accountNum && shardNum == it.shardNum && realmNum == it.realmNum
        } ?: false
    }
}