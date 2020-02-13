package hedera.hgc.hgcwallet.database.account

import androidx.room.*
import io.reactivex.Flowable


@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: Account): Long

    @Update
    fun update(vararg accounts: Account)

    @Delete
    fun delete(vararg accounts: Account)

    @Query("SELECT * FROM Account")
    fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM Account WHERE walletId=:walletId and accountType = :accountType ORDER BY creationDate ASC")
    fun findAccountForWallet(walletId: Long, accountType: String): List<Account>

    @Query("SELECT * FROM Account WHERE walletId=:walletId and accountType = :accountType ORDER BY creationDate ASC")
    fun findAccountsForWalletFlowable(walletId: Long, accountType: String): Flowable<List<Account>>

    @Query("SELECT * FROM Account WHERE keySequenceIndex=:index")
    fun findAccountForIndex(index: Long): List<Account>

    @Query("SELECT * FROM Account WHERE UID=:id")
    fun findAccountForUID(id: Long): Account
}