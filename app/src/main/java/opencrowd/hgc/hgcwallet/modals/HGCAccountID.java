package opencrowd.hgc.hgcwallet.modals;

import android.support.annotation.NonNull;

import com.hederahashgraph.api.proto.java.AccountID;

import java.util.Scanner;

import javax.annotation.Nullable;

import opencrowd.hgc.hgcwallet.database.account.Account;

public class HGCAccountID {
    private long accountNum;
    private long shardNum;
    private long realmNum;

    public HGCAccountID(long realmNum, long shardNum, long accountNum){
        this.accountNum = accountNum;
        this.shardNum = shardNum;
        this.realmNum = realmNum;
    }

    public HGCAccountID (AccountID accountID) {
        this.accountNum = accountID.getAccountNum();
        this.shardNum = accountID.getShardNum();
        this.realmNum = accountID.getRealmNum();
    }

    @NonNull
    public AccountID protoAccountID() {
        return AccountID.newBuilder()
                .setAccountNum(accountNum)
                .setShardNum(shardNum)
                .setRealmNum(realmNum).build();
    }

    @NonNull
    public String stringRepresentation() {
        return realmNum + "." + shardNum + "." + accountNum;
    }

    @Nullable
    public static HGCAccountID fromString(String string) {
        if (string == null) return null;
        String[] items = string.split("\\.");
        if (items.length == 3) {
            try {
                Long firstNum = Long.parseLong(items[0]);
                Long secondNum = Long.parseLong(items[1]);
                Long thirdNum = Long.parseLong(items[2]);
                return new HGCAccountID(firstNum, secondNum, thirdNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public long getAccountNum() {
        return accountNum;
    }

    public long getShardNum() {
        return shardNum;
    }

    public long getRealmNum() {
        return realmNum;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof HGCAccountID) {
            HGCAccountID accID = (HGCAccountID) obj;
            return accountNum == accID.accountNum && shardNum == accID.shardNum && realmNum == accID.realmNum;
        }
        return false;
    }

}