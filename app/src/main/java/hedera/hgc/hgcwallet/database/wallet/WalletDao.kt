package hedera.hgc.hgcwallet.database.wallet

import android.arch.persistence.room.*

@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wallet: Wallet): Long

    @Update
    fun update(vararg wallets: Wallet)

    @Delete
    fun delete(vararg wallets: Wallet)

    @Query("SELECT * FROM Wallet")
    fun getAllWallets(): List<Wallet>

    @Query("SELECT * FROM Wallet WHERE walletId=:walletId")
    fun findForWalletId(walletId: Long): List<Wallet>
}