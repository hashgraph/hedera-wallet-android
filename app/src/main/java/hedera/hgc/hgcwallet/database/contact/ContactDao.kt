package hedera.hgc.hgcwallet.database.contact

import androidx.room.*

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contact: Contact): Long

    @Update
    fun update(vararg contacts: Contact)

    @Delete
    fun delete(vararg contacts: Contact)

    @Query("SELECT * FROM Contact")
    fun getAllContacts(): List<Contact>

    @Query("SELECT * FROM Contact WHERE accountId=:accountId")
    fun findContactForAccountId(accountId: String): List<Contact>
}
