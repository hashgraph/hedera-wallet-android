package opencrowd.hgc.hgcwallet.hapi.tasks;

import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.TransactionRecord;

import java.util.List;

import opencrowd.hgc.hgcwallet.hapi.APIBaseTask;
import opencrowd.hgc.hgcwallet.hapi.APIRequestBuilder;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.modals.HGCAccountID;

public class UpdateTransactionsTask extends APIBaseTask {
    public UpdateTransactionsTask() {
        super();
    }

    @Override
    public void main() {
        super.main();
        List<Account> accounts = DBHelper.getAllAccounts();
        if (accounts != null && !accounts.isEmpty()) {
            Account payer = accounts.get(0);
            if (payer.accountID() == null) {
                return;
            }
            for (Account account : accounts) {
                if (account.accountID() != null) {

                    try {
                        long fee = getCostOfGetAccountRecords(payer, account.accountID());
                        Query query = APIRequestBuilder.requestForGetAccountRecord(payer, account.accountID(),fee, node.accountID());
                        log(query.toString());
                        Response response = cryptoStub().getAccountRecords(query);
                        log(response.toString());
                        ResponseCodeEnum precheck = response.getCryptogetAccountBalance().getHeader().getNodeTransactionPrecheckCode();
                        switch (precheck) {
                            case OK:
                                List<TransactionRecord> records = response.getCryptoGetAccountRecords().getRecordsList();
                                if (records != null) {
                                    for (TransactionRecord record : records) {
                                        DBHelper.createTransaction(record, account.accountID());
                                    }
                                }
                                break;
                            case INVALID_ACCOUNT_ID:
                                error = "Invalid account " + account.accountID().stringRepresentation();
                                break;

                            default:
                                error = Singleton.INSTANCE.getErrorMessage(precheck);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (error != null)
                            error = getMessage(e);
                    }
                }
            }
        }
    }

    private long getCostOfGetAccountRecords(Account payer, HGCAccountID accountID) throws Exception {
        Query query = APIRequestBuilder.requestForGetAccountRecordCost(payer, accountID, node.accountID());
        log(query.toString());
        Response response = cryptoStub().getAccountRecords(query);
        log(response.toString());
        ResponseCodeEnum precheck = response.getCryptogetAccountBalance().getHeader().getNodeTransactionPrecheckCode();
        switch (precheck) {
            case OK:
                return response.getCryptoGetAccountRecords().getHeader().getCost();

            case INVALID_ACCOUNT_ID:
                error = "Invalid account " + accountID.stringRepresentation();
                throw new Exception();

            default:
                error = Singleton.INSTANCE.getErrorMessage(precheck);
                throw new Exception();
        }
    }
}
