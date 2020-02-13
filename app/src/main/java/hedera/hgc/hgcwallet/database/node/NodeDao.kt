package hedera.hgc.hgcwallet.database.node

import androidx.room.*

@Dao
interface NodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(node: Node): Long

    @Update
    fun update(vararg nodes: Node)

    @Delete
    fun delete(vararg nodes: Node)

    @Query("DELETE FROM Node")
    fun deleteAll()

    @Query("SELECT * FROM Node")
    fun getAllNodes(): List<Node>

    @Query("SELECT * FROM Node WHERE disabled = 0")
    fun findActiveNodes(): List<Node>

    @Query("SELECT * FROM Node WHERE host=:host")
    fun findNodeForHost(host: String): List<Node>
}