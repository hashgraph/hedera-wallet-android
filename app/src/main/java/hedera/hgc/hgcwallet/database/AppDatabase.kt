/*
 *
 *  Copyright 2019-2020 Hedera Hashgraph LLC
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

package hedera.hgc.hgcwallet.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import android.content.Context

import hedera.hgc.hgcwallet.database.account.AccountDao
import hedera.hgc.hgcwallet.database.contact.Contact
import hedera.hgc.hgcwallet.database.contact.ContactDao
import hedera.hgc.hgcwallet.database.node.Node
import hedera.hgc.hgcwallet.database.node.NodeDao
import hedera.hgc.hgcwallet.database.request.PayRequest
import hedera.hgc.hgcwallet.database.request.PayRequestDao
import hedera.hgc.hgcwallet.database.transaction.TxnRecord
import hedera.hgc.hgcwallet.database.transaction.TxnRecordDao
import hedera.hgc.hgcwallet.database.wallet.WalletDao
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.database.wallet.Wallet

@Database(entities = [Wallet::class, Account::class, Contact::class, PayRequest::class,
                     TxnRecord::class, Node::class],
          version = 4)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao

    abstract fun accountDao(): AccountDao

    abstract fun contactDao(): ContactDao

    abstract fun payRequestDao(): PayRequestDao

    abstract fun txnRecordDao(): TxnRecordDao

    abstract fun nodeDao(): NodeDao

    fun clearDatabase() {
        clearAllTables()
    }

    companion object {

        fun createOrGetAppDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "wallet-database")
                    //
                    // Issue #165: Do not allow database queries on the UI thread.
                    //
                    // Allow queries on the main thread.
                    // Don't do this in a real app!  See PersistenceBasicSample for an example.
                    //
                    .allowMainThreadQueries().addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
        }


        private val MIGRATION_1_2: Migration = object: Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Account ADD COLUMN lastBalanceCheck INTEGER")
            }
        }

        private val MIGRATION_2_3: Migration = object: Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Contact " +
                        " ADD COLUMN metaData TEXT NOT NULL DEFAULT \"\" ")
                database.execSQL("ALTER TABLE Wallet " +
                        " ADD COLUMN keyDerivationType TEXT NOT NULL DEFAULT \"\" ")
            }
        }

        private val MIGRATION_3_4: Migration = object: Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE SmartContract")
                database.execSQL("" +
                        "CREATE TABLE `new_Account` (" +
                            "`walletId` INTEGER NOT NULL, " +
                            "`keyType` TEXT NOT NULL, " +
                            "`keySequenceIndex` INTEGER NOT NULL, " +
                            "`UID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`balance` INTEGER NOT NULL, " +
                            "`lastBalanceCheck` INTEGER, " +
                            "`realmNum` INTEGER NOT NULL, " +
                            "`shardNum` INTEGER NOT NULL, " +
                            "`accountNum` INTEGER NOT NULL, " +
                            "`isArchived` INTEGER NOT NULL, " +
                            "`isHidden` INTEGER NOT NULL, " +
                            "`accountType` TEXT NOT NULL DEFAULT \"auto\", " +
                            "`creationDate` INTEGER NOT NULL DEFAULT 0, " +
                            "FOREIGN KEY(`walletId`) REFERENCES `Wallet`(`walletId`) " +
                                "ON UPDATE NO ACTION " +
                                "ON DELETE CASCADE " +
                        ")")
                database.execSQL("" +
                    "INSERT INTO new_Account " +
                        "(walletId, keyType, keySequenceIndex, name, balance, lastBalanceCheck, " +
                        "realmNum, shardNum, accountNum, isArchived, isHidden)" +
                    "SELECT walletId, keyType, accountIndex, name, balance, lastBalanceCheck, " +
                        "realmNum, shardNum, accountNum, isArchived, isHidden " +
                    "FROM Account")
                database.execSQL("DROP TABLE Account")

                database.execSQL("ALTER TABLE new_Account RENAME TO Account")
            }
        }
    }
}
