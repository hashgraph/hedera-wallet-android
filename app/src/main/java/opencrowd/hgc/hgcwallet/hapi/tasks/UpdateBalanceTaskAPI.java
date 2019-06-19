package opencrowd.hgc.hgcwallet.hapi.tasks;

import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

import java.util.Date;
import java.util.List;

import opencrowd.hgc.hgcwallet.hapi.APIRequestBuilder;
import opencrowd.hgc.hgcwallet.hapi.APIBaseTask;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.account.Account;

public class UpdateBalanceTaskAPI extends APIBaseTask {

    public UpdateBalanceTaskAPI() {
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
                    Query query = APIRequestBuilder.requestForGetBalance(payer, account.accountID(), node.accountID());
                    try {
                        log(query.toString());
                        Response response = cryptoStub().cryptoGetBalance(query);
                        log(response.toString());
                        ResponseCodeEnum precheck = response.getCryptogetAccountBalance().getHeader().getNodeTransactionPrecheckCode();
                        switch (precheck) {
                            case OK:
                                long balance = response.getCryptogetAccountBalance().getBalance();
                                account.setBalance(balance);
                                account.setLastBalanceCheck(new Date());
                                DBHelper.saveAccount(account);
                                break;
                            case INVALID_ACCOUNT_ID:
                                Singleton.INSTANCE.clearAccountData(account);
                                error = "Invalid account " + account.accountID().stringRepresentation();
                                break;

                            default:
                                error = Singleton.INSTANCE.getErrorMessage(precheck);
                                break;
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        error = getMessage(e);
                    }
                }
            }
        }
    }
}
