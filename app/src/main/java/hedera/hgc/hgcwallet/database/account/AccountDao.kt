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

package hedera.hgc.hgcwallet.database.account

import androidx.room.*
import io.reactivex.Flowable


@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: Account): Long

    @Update
    fun update(vararg accounts: Account)

    @Delete
    fun delete(vararg accounts: Account)

    @Query("SELECT * FROM Account")
    fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM Account WHERE walletId=:walletId and accountType = :accountType ORDER BY creationDate ASC")
    fun findAccountForWallet(walletId: Long, accountType: String): List<Account>

    @Query("SELECT * FROM Account WHERE walletId=:walletId and accountType = :accountType ORDER BY creationDate ASC")
    fun findAccountsForWalletFlowable(walletId: Long, accountType: String): Flowable<List<Account>>

    @Query("SELECT * FROM Account WHERE keySequenceIndex=:index")
    fun findAccountForIndex(index: Long): List<Account>

    @Query("SELECT * FROM Account WHERE UID=:id")
    fun findAccountForUID(id: Long): Account
}