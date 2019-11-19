/*
 *
 *  Copyright 2019 Hedera Hashgraph LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package hedera.hgc.hgcwallet.database.transaction

import androidx.room.*

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