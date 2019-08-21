package hedera.hgc.hgcwallet.database.transaction

import android.arch.persistence.room.*

@Dao
interface TxnRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: TxnRecord): Long

    @Update
    fun update(vararg records: TxnRecord)

    @Delete
    fun delete(vararg records: TxnRecord)

    @Query("SELECT * FROM TxnRecord ORDER BY createdDate desc")
    fun getAllRecords(): List<TxnRecord>

    @Query("SELECT * FROM TxnRecord WHERE fromAccId=:accId ORDER BY createdDate desc")
    fun findRecordForAccountId(accId: String): List<TxnRecord>

    @Query("SELECT * FROM TxnRecord WHERE txnId=:txnId")
    fun findRecordForTxnId(txnId: String): List<TxnRecord>

    @Query("DELETE FROM TxnRecord")
    fun deleteAll()
}