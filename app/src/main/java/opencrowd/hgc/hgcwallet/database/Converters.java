package opencrowd.hgc.hgcwallet.database;

import android.arch.persistence.room.TypeConverter;
import android.support.annotation.Nullable;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hederahashgraph.api.proto.java.TransactionID;

import org.spongycastle.util.encoders.Hex;

import java.util.Date;

import opencrowd.hgc.hgcwallet.modals.HGCAccountID;

public class Converters {
    @Nullable
    @TypeConverter
    public static Date fromTimestamp(@Nullable Long value) {
        return value == null ? null : new Date(value);
    }

    @Nullable
    @TypeConverter
    public static Long dateToTimestamp(@Nullable Date date) {
        return date == null ? null : date.getTime();
    }
    @Nullable
    @TypeConverter
    public static String transactionIdToString(@Nullable TransactionID value) {
        if (value == null) return null;
        try {
            return Hex.toHexString(value.toByteArray()).toLowerCase();

        } catch (Exception e) {
            return null;
        }
    }
    @Nullable
    @TypeConverter
    public static TransactionID stringToTransactionID(@Nullable String value) {
        if (value == null) return null;
        try {
            return  TransactionID.parseFrom(Hex.decode(value));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
