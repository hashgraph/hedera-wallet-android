package opencrowd.hgc.hgcwallet.hapi.tasks;

import android.support.annotation.NonNull;

import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;

import opencrowd.hgc.hgcwallet.hapi.APIRequestBuilder;
import opencrowd.hgc.hgcwallet.hapi.APIBaseTask;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.database.transaction.TxnRecord;
import opencrowd.hgc.hgcwallet.modals.HGCAccountID;

public class TransferTaskAPI extends APIBaseTask {
    Account fromAccount;
    HGCAccountID toAccount;
    String notes, toAccountName;
    long amount;
    long fee;

    public TransferTaskAPI(Account fromAccount, HGCAccountID toAccount, String notes, String toAccountName, long amount, long fee) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.notes = notes;
        this.amount = amount;
        this.toAccountName = toAccountName;
        this.fee = fee;
    }

    @Override
    public void main() {
        super.main();
        DBHelper.createContact(toAccount, toAccountName, true);
        if (fromAccount.accountID() == null) {
            error = "Account is not linked";
            return;
        }
        Transaction transaction = APIRequestBuilder.requestForTransfer(fromAccount, toAccount, amount, notes, fee, node.accountID());
        try {
            log(transaction.toString());
            TransactionResponse response = cryptoStub().cryptoTransfer(transaction);
            log(response.toString());
            ResponseCodeEnum code = response.getNodeTransactionPrecheckCode();
            switch (code) {
                case OK:
                    fetchReceipt(transaction);
                    break;
                default:
                    error = Singleton.INSTANCE.getErrorMessage(code);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            error = getMessage(e);
            DBHelper.createTransaction(transaction, fromAccount.accountID());
        }
    }

    private void fetchReceipt(@NonNull Transaction transaction) {
        TxnRecord record = DBHelper.createTransaction(transaction, fromAccount.accountID());

        sleep();
        boolean success = false;
        boolean shouldBreak = false;
        for (int i = 0; i < 3; i++) {
            Query query = APIRequestBuilder.requestForGetTxnReceipt(fromAccount, APIRequestBuilder.getTxnBody(transaction).getTransactionID(), node.accountID());
            try {
                log(query.toString());
                Response response = cryptoStub().getTransactionReceipts(query);
                log(response.toString());
                if (response.getTransactionGetReceipt().hasReceipt()) {
                    record.setReceipt(response.getTransactionGetReceipt().getReceipt());
                    ResponseCodeEnum code = response.getTransactionGetReceipt().getReceipt().getStatus();
                    switch (code) {
                        case SUCCESS:
                            success = true;
                            shouldBreak = true;
                            break;
                        case UNKNOWN:
                            break;
                        default:
                            error = Singleton.INSTANCE.getErrorMessage(code);
                            shouldBreak = true;
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = getMessage(e);
            }

            if (shouldBreak) {
                break;
            }
            sleep();
        }

        if (!success && error == null) {
            error = "Fails to get receipt";
        }

        DBHelper.updateTransaction(record);

    }

    private void sleep() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
