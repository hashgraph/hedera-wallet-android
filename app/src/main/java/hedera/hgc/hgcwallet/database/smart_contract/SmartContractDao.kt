package hedera.hgc.hgcwallet.database.smart_contract

import android.arch.persistence.room.*

@Dao
interface SmartContractDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contract: SmartContract): Long

    @Update
    fun update(vararg contracts: SmartContract)

    @Delete
    fun delete(vararg contracts: SmartContract)

    @Query("SELECT * FROM SmartContract")
    fun getAllContracts(): List<SmartContract>

    @Query("SELECT * FROM SmartContract WHERE accountIndex=:accountIndex")
    fun findContactsForAccountAtIndex(accountIndex: Long): List<SmartContract>
}