package hedera.hgc.hgcwallet.database.request

import androidx.room.*

@Dao
interface PayRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(payRequest: PayRequest): Long

    @Update
    fun update(vararg payRequests: PayRequest)

    @Delete
    fun delete(vararg payRequests: PayRequest)

    @Query("SELECT * FROM PayRequest ORDER BY importDate DESC")
    fun getAllPayRequests(): List<PayRequest>

    @Query("SELECT * FROM PayRequest WHERE accountId=:accountId")
    fun findPayRequest(accountId: String): List<PayRequest>

    @Query("SELECT * FROM PayRequest WHERE accountId=:accountId AND name=:name AND amount=:amount AND notes=:notes")
    fun findPayRequest(accountId: String, name: String, amount: Long, notes: String): List<PayRequest>
}