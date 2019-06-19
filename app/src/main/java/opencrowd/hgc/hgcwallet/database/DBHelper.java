package opencrowd.hgc.hgcwallet.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionID;
import com.hederahashgraph.api.proto.java.TransactionRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import opencrowd.hgc.hgcwallet.App;
import opencrowd.hgc.hgcwallet.hapi.APIRequestBuilder;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.database.account.AccountDao;
import opencrowd.hgc.hgcwallet.database.contact.Contact;
import opencrowd.hgc.hgcwallet.database.contact.ContactDao;
import opencrowd.hgc.hgcwallet.database.node.Node;
import opencrowd.hgc.hgcwallet.database.node.NodeDao;
import opencrowd.hgc.hgcwallet.database.request.PayRequest;
import opencrowd.hgc.hgcwallet.database.request.PayRequestDao;
import opencrowd.hgc.hgcwallet.database.transaction.TxnRecord;
import opencrowd.hgc.hgcwallet.database.transaction.TxnRecordDao;
import opencrowd.hgc.hgcwallet.database.wallet.Wallet;
import opencrowd.hgc.hgcwallet.database.wallet.WalletDao;
import opencrowd.hgc.hgcwallet.modals.HGCKeyType;
import opencrowd.hgc.hgcwallet.modals.HGCAccountID;

public class DBHelper {

    public static void createMasterWallet(@NonNull HGCKeyType type) {
        WalletDao walletDao = App.instance.database.walletDao();
        Wallet wallet = null;
        List<Wallet> allWallets = walletDao.getAllWallets();
        if (allWallets != null && allWallets.size() > 0) {
            wallet = allWallets.get(0);
            // something is wrong
        } else {
            long walletId = walletDao.insert(Wallet.Companion.createWallet(type));
            createNewAccount("Default Account");
        }
    }

    @Nullable
    public static Wallet getMasterWallet() {
        WalletDao walletDao = App.instance.database.walletDao();
        Wallet wallet = null;
        List<Wallet> allWallets = walletDao.getAllWallets();
        if (allWallets != null && allWallets.size() > 0) {
            wallet = allWallets.get(0);
        }
        return wallet;
    }

    @NonNull
    public static List<Account> getAllAccounts() {
        Wallet wallet = getMasterWallet();
        AccountDao accountDao = App.instance.database.accountDao();
        List<Account> accounts = accountDao.findAccountForWallet(wallet.getWalletId());
        return accounts;
    }

    @NonNull
    public static Account createNewAccount(@NonNull String accountName) {
        WalletDao walletDao = App.instance.database.walletDao();
        AccountDao accountDao = App.instance.database.accountDao();

        Wallet wallet = getMasterWallet();
        Account account = null;
        if (wallet != null) {
            long index = wallet.getTotalAccounts();
            accountDao.insert(Account.Companion.createAccount(wallet.getWalletId(), wallet.getKeyType(), index, accountName));
            wallet.setTotalAccounts(wallet.getTotalAccounts() +1);
            walletDao.update(wallet);
            account = accountDao.findAccountForIndex(index).get(0);
        }
        return account;
    }

    public static void saveAccount(@NonNull Account account) {
        AccountDao accountDao = App.instance.database.accountDao();
        accountDao.update(account);
    }

    @Nullable
    public static List<Contact> getAllContacts() {
        ContactDao contactDao = App.instance.database.contactDao();
        return contactDao.getAllContacts();
    }

    @Nullable
    public static Contact getContact(@NonNull String accountID) {
        ContactDao contactDao = App.instance.database.contactDao();
        List<Contact> contacts = contactDao.findContactForAccountId(accountID);
        if (contacts != null && !contacts.isEmpty()) {
            return contacts.get(0);
        }
        return null;
    }

    public static void createContact(@NonNull HGCAccountID accountID, @Nullable String name, boolean isVerified) {
        if (accountID == null) return;
        if (name == null) name = "";

        ContactDao contactDao = App.instance.database.contactDao();
        Contact contact = getContact(accountID.stringRepresentation());
        if (contact == null) {
            contact = new Contact(accountID.stringRepresentation(), name, isVerified);
            contactDao.insert(contact);
        } else {
            if (name != null && contact.getName() != null) {
                if (name.equals(contact.getName()) && isVerified) {
                    contact.setVerified(true);
                } else {
                    contact.setVerified(isVerified);
                }
                contact.setName(name);
                saveContact(contact);
            }
        }
    }

    public static void saveContact(@NonNull Contact contact) {
        ContactDao contactDao = App.instance.database.contactDao();
        contactDao.update(contact);
    }

    @Nullable
    public static List<PayRequest> getAllRequests() {
        PayRequestDao payRequestDao = App.instance.database.payRequestDao();
        return payRequestDao.getAllPayRequests();
    }

    public static PayRequest getPayRequest(@NonNull HGCAccountID accountID, long amount, @Nullable String name, @Nullable String notes) {
        PayRequestDao payRequestDao = App.instance.database.payRequestDao();
        List<PayRequest> payRequests = payRequestDao.findPayRequest(accountID.stringRepresentation(), name, amount, notes);
        if (payRequests != null && !payRequests.isEmpty()) {
            return payRequests.get(0);
        }
        return null;
    }

    public static void createPayRequest(@NonNull HGCAccountID accountID, long amount, @Nullable String name, @Nullable String notes) {
        PayRequestDao payRequestDao = App.instance.database.payRequestDao();
        PayRequest payRequest = getPayRequest(accountID, amount, name, notes);
        if (payRequest == null) {
            payRequest = new PayRequest(0, accountID.stringRepresentation(), name, notes, amount, new Date());
            payRequestDao.insert(payRequest);

        } else {
            payRequest.setImportDate(new Date());
            savePayRequest(payRequest);
        }
    }

    public static void savePayRequest(@NonNull PayRequest request) {
        PayRequestDao payRequestDao = App.instance.database.payRequestDao();
        payRequestDao.update(request);
    }

    public static void deletePayRequest(@NonNull PayRequest request) {
        PayRequestDao payRequestDao = App.instance.database.payRequestDao();
        payRequestDao.delete(request);
    }

    public static TxnRecord createTransaction(@NonNull Transaction txn, HGCAccountID fromAccount) {
        TxnRecordDao dao = App.instance.database.txnRecordDao();
        TxnRecord record = getTransaction(APIRequestBuilder.getTxnBody(txn).getTransactionID());
        if (record == null) {
            record = new TxnRecord(APIRequestBuilder.getTxnBody(txn).getTransactionID());
            record.setTxn(txn.toByteArray());
            record.setCreatedDate( new Date());
            record.setFromAccId(fromAccount.stringRepresentation());
            dao.insert(record);
            return dao.findRecordForTxnId(Converters.transactionIdToString(record.getTxnId())).get(0);
        } else {
            return record;
        }
    }

    public static TxnRecord createTransaction(@NonNull TransactionRecord transactionRecord, HGCAccountID fromAccount) {
        TxnRecordDao dao = App.instance.database.txnRecordDao();
        TxnRecord record = getTransaction(transactionRecord.getTransactionID());
        if (record == null) {
            record = new TxnRecord(transactionRecord.getTransactionID());
            record.setRecord( transactionRecord.toByteArray());
            record.setCreatedDate(new Date());
            record.setFromAccId(fromAccount.stringRepresentation());
            dao.insert(record);
            return dao.findRecordForTxnId(Converters.transactionIdToString(record.getTxnId())).get(0);
        } else {
            return record;
        }
    }

    public static TxnRecord getTransaction(@NonNull TransactionID txnId) {
        String txnIdStr = Converters.transactionIdToString(txnId);
        TxnRecordDao dao = App.instance.database.txnRecordDao();
        List<TxnRecord> list = dao.findRecordForTxnId(txnIdStr);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static void updateTransaction(TxnRecord record) {
        TxnRecordDao dao = App.instance.database.txnRecordDao();
        dao.update(record);
    }

    public static List<TxnRecord> getAllTxnRecord(@Nullable Account fromAccount) {

        TxnRecordDao dao = App.instance.database.txnRecordDao();
        List<TxnRecord> records = null;
        if (fromAccount == null) {
            records = dao.getAllRecords();
        } else {
            if (fromAccount.accountID() != null) {
                records = dao.findRecordForAccountId(fromAccount.accountID().stringRepresentation());
            } else {
                records = new ArrayList<>();
            }
        }

        if (records != null && !records.isEmpty()) {
            List<Account> accounts = getAllAccounts();
            for (TxnRecord record : records) {
                record.parseProperties();
                if (record.getFromAccId() != null) {
                    Account account = getAccount(accounts, record.getFromAccId());
                    if (account != null) {
                        record.setFromAccount( account.getContact());
                    } else {
                        Contact contact = getContact(record.getFromAccId());
                        if (contact != null) {
                            record.setFromAccount( contact);
                        }
                    }
                }

                if (record.getToAccountId() != null) {
                    Account account = getAccount(accounts, record.getToAccountId());
                    if (account != null) {
                        record.setToAccount(account.getContact());
                    } else {
                        Contact contact = getContact(record.getToAccountId());
                        if (contact != null) {
                            record.setToAccount( contact);
                        }
                    }
                    if (fromAccount != null && fromAccount.accountID() != null) {
                        if (record.getToAccountId().equals(fromAccount.accountID().stringRepresentation())) {
                            record.setPositive(true);
                        }
                    }
                }
            }
        }


        return records;
    }

    public static void deleteTxnRecord(TxnRecord record) {
        TxnRecordDao dao = App.instance.database.txnRecordDao();
        dao.delete(record);
    }

    private static Account getAccount(List<Account> accounts, String accountId) {
        HGCAccountID accountID = HGCAccountID.fromString(accountId);
        if (accountID != null) {
            for (Account account : accounts) {
                HGCAccountID accId = account.accountID();
                if (accId != null && accId.stringRepresentation().equals(accountID.stringRepresentation())) {
                    return account;
                }
            }
        }
        return null;
    }

    public static Node getNode(@NonNull String host) {
        NodeDao dao = App.instance.database.nodeDao();
        List<Node> list = dao.findNodeForHost(host);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static void createNode(@NonNull Node node) {
        NodeDao dao = App.instance.database.nodeDao();
        Node record = getNode(node.getHost());
        if (record == null) {
            dao.insert(node);
        }
    }

    public static void updateNode(@NonNull Node node) {
        NodeDao dao = App.instance.database.nodeDao();
        dao.update(node);
    }

    public static List<Node> getAllNodes(Boolean activeOnly) {
        NodeDao dao = App.instance.database.nodeDao();
        if (activeOnly)
            return dao.findActiveNodes();
        else
            return dao.getAllNodes();
    }
}
