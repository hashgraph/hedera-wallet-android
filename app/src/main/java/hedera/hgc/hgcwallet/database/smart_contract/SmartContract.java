package hedera.hgc.hgcwallet.database.smart_contract;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.support.annotation.NonNull;

import hedera.hgc.hgcwallet.database.account.Account;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Account.class,
        parentColumns = "accountIndex",
        childColumns = "accountIndex", onDelete = CASCADE),
        primaryKeys = {"accountIndex","contractID"})

public class SmartContract {
    @NonNull
    public long accountIndex;
    public long balance;

    @NonNull
    public String contractID;
    public String name;
    public String symbol;
    public int decimals;
}
