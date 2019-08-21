package hedera.hgc.hgcwallet.database


import com.hederahashgraph.api.proto.java.Transaction
import com.hederahashgraph.api.proto.java.TransactionID
import com.hederahashgraph.api.proto.java.TransactionRecord

import java.util.ArrayList
import java.util.Date
import java.util.HashMap

import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.hapi.APIRequestBuilder
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.database.account.AccountDao
import hedera.hgc.hgcwallet.database.contact.Contact
import hedera.hgc.hgcwallet.database.contact.ContactDao
import hedera.hgc.hgcwallet.database.node.Node
import hedera.hgc.hgcwallet.database.node.NodeDao
import hedera.hgc.hgcwallet.database.request.PayRequest
import hedera.hgc.hgcwallet.database.request.PayRequestDao
import hedera.hgc.hgcwallet.database.transaction.TxnRecord
import hedera.hgc.hgcwallet.database.transaction.TxnRecordDao
import hedera.hgc.hgcwallet.database.wallet.Wallet
import hedera.hgc.hgcwallet.database.wallet.WalletDao
import hedera.hgc.hgcwallet.modals.HGCKeyType
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.KeyDerivation

object DBHelper {

    fun createMasterWallet(keyDerivation: KeyDerivation) {
        val walletDao = App.instance.database!!.walletDao()
        var wallet: Wallet? = null
        val allWallets = walletDao.getAllWallets()
        if (allWallets != null && allWallets.size > 0) {
            wallet = allWallets[0]
            // something is wrong
        } else {
            val walletId = walletDao.insert(Wallet.createWallet(HGCKeyType.ED25519, keyDerivation))
            createNewAccount("Default Account")
        }
    }

    fun getMasterWallet(): Wallet? {
        val walletDao = App.instance.database!!.walletDao()
        var wallet: Wallet? = null
        val allWallets = walletDao.getAllWallets()
        if (allWallets != null && allWallets.size > 0) {
            wallet = allWallets[0]
        }
        return wallet
    }

    fun getAllAccounts(): List<Account> {
        val wallet = getMasterWallet()
        val accountDao = App.instance.database!!.accountDao()
        return accountDao.findAccountForWallet(wallet!!.walletId)
    }

    fun createNewAccount(accountName: String): Account? {
        val walletDao = App.instance.database!!.walletDao()
        val accountDao = App.instance.database!!.accountDao()

        val wallet = getMasterWallet()
        var account: Account? = null
        if (wallet != null) {
            val index = wallet.totalAccounts
            accountDao.insert(Account.createAccount(wallet.walletId, wallet.keyType, index, accountName))
            wallet.totalAccounts = wallet.totalAccounts + 1
            walletDao.update(wallet)
            account = accountDao.findAccountForIndex(index)[0]
        }
        return account
    }

    fun saveAccount(account: Account) {
        val accountDao = App.instance.database!!.accountDao()
        accountDao.update(account)
    }

    fun getAllContacts(): List<Contact>? {
        val contactDao = App.instance.database!!.contactDao()
        return contactDao.getAllContacts()
    }

    fun getContact(accountID: String): Contact? {
        val contactDao = App.instance.database!!.contactDao()
        val contacts = contactDao.findContactForAccountId(accountID)
        return if (contacts != null && !contacts.isEmpty()) {
            contacts[0]
        } else null
    }

    fun createContact(accountID: HGCAccountID, name: String?, host: String, isVerified: Boolean) {
        var name = name
        if (accountID == null) return
        if (name == null) name = ""

        val contactDao = App.instance.database!!.contactDao()
        var contact = getContact(accountID.stringRepresentation())
        val converter = Converters()
        if (contact == null) {
            var metaData = ""
            if (!host.isEmpty()) {
                val metaDataMap = HashMap<String, String>()
                metaDataMap["host"] = host
                metaData = converter.fromStringMap(metaDataMap)
            }
            contact = Contact(accountID.stringRepresentation(), name, isVerified, metaData)
            contactDao.insert(contact)
        } else {
            if (!host.isEmpty()) {
                val metaDataMap: MutableMap<String, String>
                if (contact.metaData.isEmpty())
                    metaDataMap = HashMap()
                else
                    metaDataMap = converter.fromString(contact.metaData).toMutableMap()

                metaDataMap["host"] = host
                contact.metaData = converter.fromStringMap(metaDataMap)
            }

            if (name != null && contact.name != null) {
                if (name == contact.name && isVerified) {
                    contact.isVerified = true
                } else {
                    contact.isVerified = isVerified
                }
                contact.name = name
                saveContact(contact)
            }
        }
    }

    fun saveContact(contact: Contact) {
        val contactDao = App.instance.database!!.contactDao()
        contactDao.update(contact)
    }

    fun getAllRequests(): List<PayRequest>? {
        val payRequestDao = App.instance.database!!.payRequestDao()
        return payRequestDao.getAllPayRequests()
    }

    fun getPayRequest(accountID: HGCAccountID, amount: Long, name: String?, notes: String?): PayRequest? {
        val payRequestDao = App.instance.database!!.payRequestDao()
        val payRequests = payRequestDao.findPayRequest(accountID.stringRepresentation(), name!!, amount, notes!!)
        return if (payRequests != null && !payRequests.isEmpty()) {
            payRequests[0]
        } else null
    }

    fun createPayRequest(accountID: HGCAccountID, amount: Long, name: String?, notes: String?) {
        val payRequestDao = App.instance.database!!.payRequestDao()
        var payRequest = getPayRequest(accountID, amount, name, notes)
        if (payRequest == null) {
            payRequest = PayRequest(0, accountID.stringRepresentation(), name, notes, amount, Date())
            payRequestDao.insert(payRequest)

        } else {
            payRequest.importDate = Date()
            savePayRequest(payRequest)
        }
    }

    fun savePayRequest(request: PayRequest) {
        val payRequestDao = App.instance.database!!.payRequestDao()
        payRequestDao.update(request)
    }

    fun deletePayRequest(request: PayRequest) {
        val payRequestDao = App.instance.database!!.payRequestDao()
        payRequestDao.delete(request)
    }


    fun createTransaction(txn: Transaction, fromAccount: HGCAccountID?): TxnRecord {
        val dao = App.instance.database!!.txnRecordDao()
        var record = getTransaction(APIRequestBuilder.getTxnBody(txn).transactionID)
        if (record == null) {
            record = TxnRecord(APIRequestBuilder.getTxnBody(txn).transactionID)
            record.txn = txn.toByteArray()
            record.createdDate = Date()
            fromAccount?.let { record.fromAccId = it.stringRepresentation() }
            dao.insert(record)
            return dao.findRecordForTxnId(Converters().transactionIdToString(record.txnId)!!)[0]
        } else {
            return record
        }
    }

    fun createTransaction(transactionRecord: TransactionRecord, fromAccount: HGCAccountID?): TxnRecord {
        val dao = App.instance.database!!.txnRecordDao()
        var record = getTransaction(transactionRecord.transactionID)
        if (record == null) {
            record = TxnRecord(transactionRecord.transactionID)
            record.record = transactionRecord.toByteArray()
            record.createdDate = Date()
            fromAccount?.let { record.fromAccId = it.stringRepresentation() }
            dao.insert(record)
            return dao.findRecordForTxnId(Converters().transactionIdToString(record.txnId)!!)[0]
        } else {
            return record
        }
    }

    fun getTransaction(txnId: TransactionID): TxnRecord? {
        val txnIdStr = Converters().transactionIdToString(txnId)
        val dao = App.instance.database!!.txnRecordDao()
        val list = dao.findRecordForTxnId(txnIdStr!!)
        return if (list != null && !list.isEmpty()) {
            list[0]
        } else null
    }

    fun updateTransaction(record: TxnRecord) {
        val dao = App.instance.database!!.txnRecordDao()
        dao.update(record)
    }

    fun getAllTxnRecord(fromAccount: Account?): List<TxnRecord> {

        val dao = App.instance.database!!.txnRecordDao()
        var records: List<TxnRecord>? = null
        if (fromAccount == null) {
            records = dao.getAllRecords()
        } else {
            if (fromAccount.accountID() != null) {
                records = dao.findRecordForAccountId(fromAccount.accountID()!!.stringRepresentation())
            } else {
                records = ArrayList()
            }
        }

        if (records != null && !records.isEmpty()) {
            val accounts = getAllAccounts()
            for (record in records) {
                record.parseProperties()
                if (record.fromAccId != null) {
                    val account = getAccount(accounts, record.fromAccId)
                    if (account != null) {
                        record.fromAccount = account!!.getContact()
                    } else {
                        val contact = getContact(record.fromAccId!!)
                        if (contact != null) {
                            record.fromAccount = contact
                        }
                    }
                }

                if (record.toAccountId != null) {
                    val account = getAccount(accounts, record.toAccountId)
                    if (account != null) {
                        record.toAccount = account!!.getContact()
                    } else {
                        val contact = getContact(record.toAccountId!!)
                        if (contact != null) {
                            record.toAccount = contact
                        }
                    }
                    if (fromAccount != null && fromAccount.accountID() != null) {
                        if (record.toAccountId == fromAccount.accountID()!!.stringRepresentation()) {
                            record.isPositive = true
                        }
                    }
                }
            }
        }


        return records
    }

    fun deleteTxnRecord(record: TxnRecord) {
        val dao = App.instance.database!!.txnRecordDao()
        dao.delete(record)
    }

    private fun getAccount(accounts: List<Account>, accountId: String?): Account? {
        val accountID = HGCAccountID.fromString(accountId)
        if (accountID != null) {
            for (account in accounts) {
                val accId = account.accountID()
                if (accId != null && accId.stringRepresentation() == accountID.stringRepresentation()) {
                    return account
                }
            }
        }
        return null
    }

    fun getNode(host: String): Node? {
        val dao = App.instance.database!!.nodeDao()
        val list = dao.findNodeForHost(host)
        return if (list != null && !list.isEmpty()) {
            list[0]
        } else null
    }

    fun createNode(node: Node) {
        val dao = App.instance.database!!.nodeDao()
        val record = getNode(node.host!!)
        if (record == null) {
            dao.insert(node)
        }
    }

    fun updateNode(node: Node) {
        val dao = App.instance.database!!.nodeDao()
        dao.update(node)
    }

    fun getAllNodes(activeOnly: Boolean): List<Node> {
        val dao = App.instance.database!!.nodeDao()
        return if (activeOnly)
            dao.findActiveNodes()
        else
            dao.getAllNodes()
    }


    fun deleteAllNodes() {
        val dao = App.instance.database!!.nodeDao()
        dao.deleteAll()
    }

    fun updateWallet(wallet: Wallet?) {
        val walletDao = App.instance.database!!.walletDao()
        if (wallet != null)
            walletDao.update(wallet)

    }
}