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
import android.database.SQLException

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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Database(entities = [Wallet::class, Account::class, Contact::class, PayRequest::class,
                     TxnRecord::class, Node::class],
          version = 5)
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
        // waiting 1 sec to allow clearing tables to finish.
        Thread.sleep(1_000);
    }

    companion object: RoomDatabase.Callback() {

        private const val latestVersion: Int = 5

        val Log: Logger = LoggerFactory.getLogger(this::class.java)

        fun createOrGetAppDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "wallet-database")
                    //
                    // Issue #165: Do not allow database queries on the UI thread.
                    //
                    // Allow queries on the main thread.
                    // Don't do this in a real app!  See PersistenceBasicSample for an example.
                    //
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
        }


        private val MIGRATION_1_2: Migration = object: Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.debug("Beginning DB migration from version 1 to 2")
                try {
                    database.execSQL("ALTER TABLE Account ADD COLUMN lastBalanceCheck INTEGER")
                    Log.debug("PASSED DB migration")
                } catch (e: SQLException) {
                    Log.error("SQL exception: $e")
                    Log.error("FAILED DB migration")
                    throw e
                } catch (e: RuntimeException) {
                    Log.error("Runtime exception: $e")
                    Log.error("FAILED DB migration")
                    throw e
                } finally {
                    Log.debug("Ending DB migration from version 1 to 2")
                }
            }
        }

        private val MIGRATION_2_3: Migration = object: Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.debug("Beginning DB migration from version 2 to 3")
                try {
                    database.execSQL("ALTER TABLE Contact " +
                            " ADD COLUMN metaData TEXT NOT NULL DEFAULT \"\" ")
                    database.execSQL("ALTER TABLE Wallet " +
                            " ADD COLUMN keyDerivationType TEXT NOT NULL DEFAULT \"\" ")
                } catch (e: SQLException) {
                    Log.error("SQL exception: $e")
                    Log.error("FAILED DB migration")
                    throw e
                } catch (e: RuntimeException) {
                    Log.error("Runtime exception: $e")
                    Log.error("FAILED DB migration")
                    throw e
                } finally {
                    Log.debug("Ending DB migration from version 2 to 3")
                }
            }
        }

        private val MIGRATION_3_4: Migration = object: Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.debug("Beginning DB migration from version 3 to 4")
                try {
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
                } catch (e: SQLException) {
                    Log.error("SQL exception: $e")
                    Log.error("FAILED DB migration")
                    throw e
                } catch (e: RuntimeException) {
                    Log.error("Runtime exception: $e")
                    Log.error("FAILED DB migration")
                    throw e
                } finally {
                    Log.debug("Ending DB migration from version 3 to 4")
                }
            }
        }

        private val MIGRATION_4_5: Migration = object: Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.debug("Beginning DB migration from version 4 to 5")
                try {
                    //
                    // Issue #157: Update address book to include new nodes
                    //
                    // Remove all of the current nodes, forcing the address book to be reloaded.
                    // This is the minimal change needed, and we need to minimize changes until
                    // test infrastructure is better established.
                    //
                    database.execSQL("DELETE FROM NODE")
                } catch (e: SQLException) {
                    Log.error("SQL exception: $e")
                    Log.error("FAILED DB migration")
                    throw e
                } catch (e: RuntimeException) {
                    Log.error("Runtime exception: $e")
                    Log.error("FAILED DB migration")
                    throw e
                } finally {
                    Log.debug("Ending DB migration from version 4 to 5")
                }
            }
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.info("Database created")
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            Log.info("Database opened")
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            Log.info("Database destructively migrated to version $latestVersion")
        }
    }
}
