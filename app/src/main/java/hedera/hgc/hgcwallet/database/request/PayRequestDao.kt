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