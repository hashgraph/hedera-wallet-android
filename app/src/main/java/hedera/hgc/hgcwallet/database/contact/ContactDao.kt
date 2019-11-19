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
