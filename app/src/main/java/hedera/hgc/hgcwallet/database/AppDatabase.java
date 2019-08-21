package hedera.hgc.hgcwallet.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import hedera.hgc.hgcwallet.database.account.AccountDao;
import hedera.hgc.hgcwallet.database.contact.Contact;
import hedera.hgc.hgcwallet.database.contact.ContactDao;
import hedera.hgc.hgcwallet.database.node.Node;
import hedera.hgc.hgcwallet.database.node.NodeDao;
import hedera.hgc.hgcwallet.database.request.PayRequest;
import hedera.hgc.hgcwallet.database.request.PayRequestDao;
import hedera.hgc.hgcwallet.database.smart_contract.SmartContract;
import hedera.hgc.hgcwallet.database.smart_contract.SmartContractDao;
import hedera.hgc.hgcwallet.database.transaction.TxnRecord;
import hedera.hgc.hgcwallet.database.transaction.TxnRecordDao;
import hedera.hgc.hgcwallet.database.wallet.WalletDao;
import hedera.hgc.hgcwallet.database.account.Account;
import hedera.hgc.hgcwallet.database.wallet.Wallet;

@Database(entities = {
        Wallet.class,
        Account.class,
        Contact.class,
        PayRequest.class,
        TxnRecord.class,
        SmartContract.class,
        Node.class},
        version = 3)

@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract WalletDao walletDao();

    public abstract AccountDao accountDao();

    public abstract ContactDao contactDao();

    public abstract PayRequestDao payRequestDao();

    public abstract TxnRecordDao txnRecordDao();

    public abstract NodeDao nodeDao();

    public abstract SmartContractDao smartContractDao();

    public static AppDatabase createOrGetAppDatabase(@NonNull Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "wallet-database")
                // allow queries on the main thread.
                // Don't do this on a real app! See PersistenceBasicSample for an example.
                .allowMainThreadQueries().addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build();
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Account "
                    + " ADD COLUMN lastBalanceCheck INTEGER");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Contact "
                    + " ADD COLUMN metaData TEXT NOT NULL DEFAULT \"\" ");
            database.execSQL("ALTER TABLE Wallet "
                    + " ADD COLUMN keyDerivationType TEXT NOT NULL DEFAULT \"\" ");
        }
    };
}
