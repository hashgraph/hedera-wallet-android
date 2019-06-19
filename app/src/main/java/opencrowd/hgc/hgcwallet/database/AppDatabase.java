package opencrowd.hgc.hgcwallet.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import opencrowd.hgc.hgcwallet.database.account.AccountDao;
import opencrowd.hgc.hgcwallet.database.contact.Contact;
import opencrowd.hgc.hgcwallet.database.contact.ContactDao;
import opencrowd.hgc.hgcwallet.database.node.Node;
import opencrowd.hgc.hgcwallet.database.node.NodeDao;
import opencrowd.hgc.hgcwallet.database.request.PayRequest;
import opencrowd.hgc.hgcwallet.database.request.PayRequestDao;
import opencrowd.hgc.hgcwallet.database.smart_contract.SmartContract;
import opencrowd.hgc.hgcwallet.database.smart_contract.SmartContractDao;
import opencrowd.hgc.hgcwallet.database.transaction.TxnRecord;
import opencrowd.hgc.hgcwallet.database.transaction.TxnRecordDao;
import opencrowd.hgc.hgcwallet.database.wallet.WalletDao;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.database.wallet.Wallet;

@Database(entities = {
        Wallet.class,
        Account.class,
        Contact.class,
        PayRequest.class,
        TxnRecord.class,
        SmartContract.class,
        Node.class},
        version = 2)

@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract WalletDao walletDao();
    public abstract AccountDao accountDao();
    public abstract ContactDao contactDao();
    public abstract PayRequestDao payRequestDao();
    public abstract TxnRecordDao txnRecordDao();
    public abstract NodeDao nodeDao();
    public abstract SmartContractDao smartContractDao();

    public  static AppDatabase createOrGetAppDatabase(@NonNull Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "wallet-database")
                // allow queries on the main thread.
                // Don't do this on a real app! See PersistenceBasicSample for an example.
                .allowMainThreadQueries().addMigrations(MIGRATION_1_2)
                .build();
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Account "
                    + " ADD COLUMN lastBalanceCheck INTEGER");
        }
    };
}
