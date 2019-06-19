package opencrowd.hgc.hgcwallet.hapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hederahashgraph.api.proto.java.AccountAmount;
import com.hederahashgraph.api.proto.java.AccountID;
import com.hederahashgraph.api.proto.java.CryptoGetAccountBalanceQuery;
import com.hederahashgraph.api.proto.java.CryptoGetAccountRecordsQuery;
import com.hederahashgraph.api.proto.java.CryptoTransferTransactionBody;
import com.hederahashgraph.api.proto.java.Duration;
import com.hederahashgraph.api.proto.java.FeeComponents;
import com.hederahashgraph.api.proto.java.FeeData;
import com.hederahashgraph.api.proto.java.FeeSchedule;
import com.hederahashgraph.api.proto.java.HederaFunctionality;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.ResponseType;
import com.hederahashgraph.api.proto.java.Signature;
import com.hederahashgraph.api.proto.java.SignatureList;
import com.hederahashgraph.api.proto.java.SignatureMap;
import com.hederahashgraph.api.proto.java.SignaturePair;
import com.hederahashgraph.api.proto.java.Timestamp;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionFeeSchedule;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptQuery;
import com.hederahashgraph.api.proto.java.TransactionID;
import com.hederahashgraph.api.proto.java.TransferList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opencrowd.hgc.hgcwallet.App;
import opencrowd.hgc.hgcwallet.Config;
import opencrowd.hgc.hgcwallet.hapi.fee.CryptoFeeBuilder;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.crypto.KeyPair;
import opencrowd.hgc.hgcwallet.modals.HGCAccountID;

public class APIRequestBuilder {

    public static long PLACEHOLDE_FEE = -10000;

    public static Map<HederaFunctionality,FeeData> getFeeMap() {
        try {
            InputStream is = App.instance.getAssets().open("feeScheduleproto.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Get the FeeSchedule Object
            FeeSchedule feeSch = FeeSchedule.parseFrom(buffer);
            Map<HederaFunctionality,FeeData> feeSchMap = new HashMap<>();
            List<TransactionFeeSchedule> transFeeSchList = feeSch.getTransactionFeeScheduleList();

            for(TransactionFeeSchedule transSch : transFeeSchList ) {
                feeSchMap.put(transSch.getHederaFunctionality(), transSch.getFeeData());
            }

            return feeSchMap;

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static long feeForTransferTransaction(Transaction txn) {
        // get the FeeData for specific functionality from map
        Map<HederaFunctionality,FeeData> feeSchMap = getFeeMap();
        FeeData feeData = feeSchMap.get(HederaFunctionality.CryptoTransfer);
        CryptoFeeBuilder crBuilder = new CryptoFeeBuilder();

        // get the FeeMatrices for specific functionlaity
        FeeComponents feeMatrics = crBuilder.getCryptoTransferTxFeeMatrices(txn);
        // pass the FeeData and generated Matrices and get the Fee
        long schFee = crBuilder.getTotalFeeforRequest(feeData, feeMatrics);
        return schFee;
    }

    public static long feeForGetBalance() {
        // get the FeeData for specific functionality from map
        Map<HederaFunctionality,FeeData> feeSchMap = getFeeMap();
        FeeData feeData = feeSchMap.get(HederaFunctionality.CryptoGetAccountBalance);
        CryptoFeeBuilder crBuilder = new CryptoFeeBuilder();

        // get the FeeMatrices for specific functionlaity
        FeeComponents feeMatrics = crBuilder.getBalanceQueryFeeMatrices();
        // pass the FeeData and generated Matrices and get the Fee
        long schFee = crBuilder.getTotalFeeforRequest(feeData, feeMatrics);
        return schFee;
    }

    public static long feeForGetAccountRecordCostQuery() {
        // get the FeeData for specific functionality from map
        Map<HederaFunctionality,FeeData> feeSchMap = getFeeMap();
        FeeData feeData = feeSchMap.get(HederaFunctionality.CryptoGetAccountRecords);
        CryptoFeeBuilder crBuilder = new CryptoFeeBuilder();

        // get the FeeMatrices for specific functionlaity
        FeeComponents feeMatrics = crBuilder.getCostCryptoAccountRecordsQueryFeeMatrices();
        // pass the FeeData and generated Matrices and get the Fee
        long schFee = crBuilder.getTotalFeeforRequest(feeData, feeMatrics);
        return schFee;
    }

    public static Query requestForGetBalance(@NonNull Account payer, @NonNull HGCAccountID account, @NonNull HGCAccountID node){
        QueryHeader header = createQueryHeader(payer, "for get balance", true, feeForGetBalance(), ResponseType.ANSWER_ONLY,node);
        CryptoGetAccountBalanceQuery getAccountBalanceQuery = CryptoGetAccountBalanceQuery.newBuilder()
                .setHeader(header)
                .setAccountID(account.protoAccountID()).build();
        Query query = Query.newBuilder().setCryptogetAccountBalance(getAccountBalanceQuery).build();
        return query;
    }

    public static  Transaction requestForTransfer(@NonNull Account from, @NonNull HGCAccountID toAccount, long amount, @Nullable String notes, long fee, @NonNull HGCAccountID node) {
        TransactionBody.Builder body = cretaeTxnBody(from, notes,true,fee, node);
        body.setCryptoTransfer(createTransferBody(from, toAccount, amount));
        Transaction txn = createSignedTransaction(from, body.build());
        return txn;
    }

    public static Query requestForGetTxnReceipt(@NonNull Account payer, @NonNull TransactionID txnId, @NonNull HGCAccountID node){
        QueryHeader header = createQueryHeader(payer, "for get receipt",false, 0, ResponseType.ANSWER_ONLY, node);
        TransactionGetReceiptQuery.Builder builder = TransactionGetReceiptQuery.newBuilder();
        builder.setHeader(header);
        builder.setTransactionID(txnId);
        Query query = Query.newBuilder().setTransactionGetReceipt(builder.build()).build();
        return query;
    }

    public static Query requestForGetAccountRecordCost(@NonNull Account payer, @NonNull HGCAccountID accountID, @NonNull HGCAccountID node){
        QueryHeader header = createQueryHeader(payer, "for get account record cost", true, feeForGetAccountRecordCostQuery(), ResponseType.COST_ANSWER, node);
        CryptoGetAccountRecordsQuery.Builder builder = CryptoGetAccountRecordsQuery.newBuilder();
        builder.setHeader(header);
        builder.setAccountID(accountID.protoAccountID());
        Query query = Query.newBuilder().setCryptoGetAccountRecords(builder).build();
        return query;
    }

    public static Query requestForGetAccountRecord(@NonNull Account payer, @NonNull HGCAccountID accountID, long fee, @NonNull HGCAccountID node){
        QueryHeader header = createQueryHeader(payer, "for get account record", true, fee, ResponseType.ANSWER_ONLY, node);
        CryptoGetAccountRecordsQuery.Builder builder = CryptoGetAccountRecordsQuery.newBuilder();
        builder.setHeader(header);
        builder.setAccountID(accountID.protoAccountID());
        Query query = Query.newBuilder().setCryptoGetAccountRecords(builder).build();
        return query;
    }

    private static QueryHeader createQueryHeader(Account payer, String memo, boolean includePayment, long fee, ResponseType rType, HGCAccountID node) {
        AccountID payerAccount = payer.accountID().protoAccountID();
        AccountID nodeAccount = node.protoAccountID();

        CryptoTransferTransactionBody transferTransactionBody = createTransferBody(payer, node, fee).build();
        TransactionBody body = cretaeTxnBody(payer, memo, PLACEHOLDE_FEE, node).setCryptoTransfer(transferTransactionBody).build();
        Transaction txn = createSignedTransaction(payer, body);

        long feeForTransfer = feeForTransferTransaction(txn);
        body = cretaeTxnBody(payer, memo, feeForTransfer, node).setCryptoTransfer(transferTransactionBody).build();
        txn = createSignedTransaction(payer, body);

        QueryHeader.Builder builder = QueryHeader.newBuilder();
        if (includePayment) {
            builder.setPayment(txn);
        }
        builder.setResponseType(rType);
        QueryHeader header = builder.build();
        return  header;
    }

    private static Transaction createSignedTransaction(@NonNull Account payer, @NonNull TransactionBody body) {
        if (Config.useBetaAPIs) {
            SignaturePair signature = createSignaturePair(payer, body);
            SignatureMap signatureMap = SignatureMap.newBuilder().addSigPair(signature).build();
            Transaction txn = Transaction.newBuilder().setBodyBytes(body.toByteString()).setSigMap(signatureMap).build();
            return txn;
        } else  {
            Signature signature = createSignature(payer, body);
            SignatureList list = SignatureList.newBuilder().addSigs(signature).addSigs(signature).build();
            Transaction txn = Transaction.newBuilder().setBody(body).setSigs(list).build();
            return txn;
        }

    }

    private static SignaturePair createSignaturePair(@NonNull Account account, @NonNull TransactionBody body) {
        SignaturePair.Builder builder = SignaturePair.newBuilder();
        KeyPair keyPair = Singleton.INSTANCE.keyForAccount(account);
        builder.setPubKeyPrefix(ByteString.copyFrom(keyPair.getPublicKey(), 0, 4));
        byte[] s = keyPair.signMessage(body.toByteArray());
        ByteString bs = ByteString.copyFrom(s);
        switch (account.getHGCKeyType()) {
            case ECDSA384:
                builder.setECDSA384(bs);
                break;
            case ED25519:
                builder.setEd25519(bs);
                break;
            case RSA3072:
                builder.setRSA3072(bs);
                break;
        }

        return  builder.build();
    }

    private static Signature createSignature(@NonNull Account account, @NonNull TransactionBody body) {
        Signature.Builder builder = Signature.newBuilder();
        ByteString bs = null;
        if (body.getTransactionFee() != PLACEHOLDE_FEE) {
            KeyPair keyPair = Singleton.INSTANCE.keyForAccount(account);
            byte[] s = keyPair.signMessage(body.toByteArray());
            bs = ByteString.copyFrom(s);
        } else {
            bs = ByteString.copyFromUtf8("");
        }

        switch (account.getHGCKeyType()) {
            case ECDSA384:
                builder.setECDSA384(bs);
                break;
            case ED25519:
                builder.setEd25519(bs);
                break;
            case RSA3072:
                builder.setRSA3072(bs);
                break;
        }
        return  builder.build();

//        SignatureList signatureList = SignatureList.newBuilder().addSigs(builder.build()).build();
//        return Signature.newBuilder().setSignatureList(signatureList).build();
    }

    private static TransactionBody.Builder cretaeTxnBody(Account payer,String memo, long fee, HGCAccountID node) {
        return cretaeTxnBody(payer,memo, false, fee, node);
    }

    private static TransactionBody.Builder cretaeTxnBody(Account payer,String memo, Boolean genRecord, long fee, HGCAccountID node) {
        AccountID payerAccount = payer.accountID().protoAccountID();
        AccountID nodeAccount = node.protoAccountID();
        Timestamp timestamp = createTimestamp(new Date());

        TransactionID transactionID = TransactionID.newBuilder()
                .setAccountID(payerAccount)
                .setTransactionValidStart(timestamp).build();
        return TransactionBody.newBuilder()
                .setTransactionID(transactionID)
                .setTransactionFee(fee)
                .setGenerateRecord(genRecord)
                .setMemo(memo)
                .setTransactionValidDuration(createDuration(120000))
                .setNodeAccountID(nodeAccount);
    }

    private static CryptoTransferTransactionBody.Builder createTransferBody(Account from, HGCAccountID to, long amount) {
        AccountAmount accountAmount1 = AccountAmount.newBuilder().setAmount(amount*-1).setAccountID(from.accountID().protoAccountID()).build();
        AccountAmount accountAmount2 = AccountAmount.newBuilder().setAmount(amount).setAccountID(to.protoAccountID()).build();
        TransferList list = TransferList.newBuilder().addAccountAmounts(accountAmount1).addAccountAmounts(accountAmount2).build();
        return CryptoTransferTransactionBody.newBuilder().setTransfers(list);

    }

    private static Timestamp createTimestamp(Date date) {
        long a = 1000;
        long millis = date.getTime();
        /*  it look like there is some offset added sometime during the day time or so,
            that causes start time validation failure at serivce side
            delaying by 15 seconds fixed it
        */
        millis -= (15*1000);
        long seconds = millis/a;
        int n = (int) ((millis - seconds * a)*1000000);
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(seconds).setNanos(n).build();
        return timestamp;
    }

    private static Duration createDuration(long millis) {
        long a = 1000;
        long seconds = millis/a;
        int n = 0;
        Duration duration = Duration.newBuilder().setSeconds(seconds).build();
        return duration;
    }

    public static TransactionBody getTxnBody(Transaction transaction) {
        ByteString bodyBytes = transaction.getBodyBytes();
        if (bodyBytes != null && bodyBytes.size() > 0) {
            try {
                return TransactionBody.parseFrom(bodyBytes);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return  transaction.getBody();
    }
}