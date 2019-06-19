package opencrowd.hgc.hgcwallet.database.account

import android.arch.persistence.room.*

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: Account)

    @Update
    fun update(vararg accounts: Account)

    @Delete
    fun delete(vararg accounts: Account)

    @Query("SELECT * FROM Account")
    fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM Account WHERE walletId=:walletId")
    fun findAccountForWallet(walletId: Long): List<Account>

    @Query("SELECT * FROM Account WHERE accountIndex=:index")
    fun findAccountForIndex(index: Long): List<Account>
}