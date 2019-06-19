package opencrowd.hgc.hgcwallet.hapi.fee;

import com.hederahashgraph.api.proto.java.*;

import java.util.List;

import opencrowd.hgc.hgcwallet.hapi.APIRequestBuilder;


/**
 * This class includes methods for generating Fee Matrices and calculating
 * Fee for Crypto related Transactions and Query.
 */

public class CryptoFeeBuilder extends FeeBuilder {


	 /**
	  * This method returns the Fee Matrices for Crypto Create Transaction.
	  *
	  * @param transaction
	  * @return
	  */
	 public FeeComponents getCryptoCreateTxFeeMatrices(Transaction transaction) {

		  if (transaction == null) {
				return getDefaultMatrices();
		  }
		 TransactionBody transactionBody = APIRequestBuilder.getTxnBody(transaction);

		  long bpt = 0;
		  long vpt = 0;
		  long rbs = 0;
		  long sbs = 0;
		  long gas = 0;
		  long tv = 0;
		  long bpr = 0;
		  long sbpr = 0;

		  // calculate BPT - Total Bytes in Transaction
		  int txBodySize = 0;
		  txBodySize = getCommonTransactionBodyBytes(transaction);
		  bpt = txBodySize + getCryptoCreateAccountBodyTxSize(transactionBody);

		  //vpt - verifications per transactions
		  vpt = getVPT(transaction);

		  //rbs - RAM bytes seconds
		  rbs = getCryptoRBS(transactionBody);
		  //sbs - Stoarge bytes seconds
		  sbs = getCryptoCreateStorageBytesSec(transactionBody);


		  FeeComponents feeMatricesForTx = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs).setSbs(sbs)
					 .setGas(gas).setTv(tv).setBpr(bpr).setSbpr(sbpr)
					 .build();

		  return feeMatricesForTx;
	 }


	 /**
	  * This method returns the Fee Matrices for Crypto Transfer Transaction.
	  *
	  * @param transaction
	  * @return
	  */
	 public FeeComponents getCryptoTransferTxFeeMatrices(Transaction transaction) {

		  if (transaction == null) {
				return getDefaultMatrices();
		  }

		 TransactionBody transactionBody = APIRequestBuilder.getTxnBody(transaction);

		 long bpt = 0;
		  long vpt = 0;
		  long rbs = 0;
		  long sbs = 0;
		  long gas = 0;
		  long tv = 0;
		  long bpr = 0;
		  long sbpr = 0;

		  int txBodySize = 0;
		  txBodySize = getCommonTransactionBodyBytes(transaction);

		  //bpt - Bytes per Transaction
		  bpt = txBodySize + getCryptoTransferBodyTxSize(transactionBody);

		  //vpt - verifications per transactions
		  vpt = getVPT(transaction);

		  //tv - Transfer Value
		  tv = getTV(transactionBody);

		  FeeComponents feeMatricesForTx = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs).setSbs(sbs)
					 .setGas(gas).setTv(tv).setBpr(bpr).setSbpr(sbpr)
					 .build();

		  return feeMatricesForTx;

	 }

	 /**
	  * This method returns the Fee Matrices for Crypto Update Transaction
	  *
	  * @param transaction
	  * @param expirationTimeStamp
	  * @return
	  */
	 /*
	 public FeeComponents getCryptoUpdateTxFeeMatrices(Transaction transaction, Timestamp expirationTimeStamp) {

		  if (transaction == null) {
				return getDefaultMatrices();
		  }

		  long bpt = 0;
		  long vpt = 0;
		  long rbs = 0;
		  long sbs = 0;
		  long gas = 0;
		  long tv = 0;
		  long bpr = 0;
		  long sbpr = 0;
		  CryptoUpdateTransactionBody crUpdateTxBody = transaction.getBody().getCryptoUpdateAccount();
		  int txBodySize = 0;
		  txBodySize = getCommonTransactionBodyBytes(transaction);

		  //bpt - Bytes per Transaction
		  bpt = txBodySize + getCryptoUpdateBodyTxSize(transaction);

		  //vpt - verifications per transactions
		  vpt = getVPT(transaction);

		  //sbs - Storage bytes seconds - check if key is changed, need to charge for new key storage
		  if (crUpdateTxBody.getKey() != null) {
				int newKeySize = getAccountKeyStorageSize(crUpdateTxBody.getKey());
				Instant expirationTime = RequestBuilder.convertProtoTimeStamp(expirationTimeStamp);
				Timestamp txValidStartTimestamp = transaction.getBody().getTransactionID().getTransactionValidStart();
				Instant txValidStartTime = RequestBuilder.convertProtoTimeStamp(txValidStartTimestamp);
				Duration duration = Duration.between(expirationTime, txValidStartTime);
				long seconds = duration.getSeconds();
				sbs = newKeySize * seconds;
		  }

		  FeeComponents feeMatricesForTx = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs).setSbs(sbs)
					 .setGas(gas).setTv(tv).setBpr(bpr).setSbpr(sbpr)
					 .build();

		  return feeMatricesForTx;
	 }
*/
	 /**
	  * This method calculated total bytes in Crypto Update Tx body
	  *
	  * @param tx
	  * @return
	  */
//	 private int getCryptoUpdateBodyTxSize(Transaction tx) {
//		/*
//		 *  AccountID accountIDToUpdate - 3 * LONG_SIZE
//		    Key -       calculated bytes
//		    AccountID proxyAccountID - 3 * LONG_SIZE
//		    int32 proxyFraction - INT_SIZE
//		    uint64 sendRecordThreshold - LONG_SIZE
//		    uint64 receiveRecordThreshold - LONG_SIZE
//		    Duration autoRenewPeriod - (LONG_SIZE + INT_SIZE)
//		    Timestamp expirationTime - (LONG_SIZE + INT_SIZE) bytes
//		 */
//
//		  int cryptoAcctUpdateBodySize = 3 * LONG_SIZE;
//		  CryptoUpdateTransactionBody crUpdateTxBody = tx.getBody().getCryptoUpdateAccount();
//
//		  if (crUpdateTxBody.getKey() != null) {
//				cryptoAcctUpdateBodySize += getAccountKeyStorageSize(crUpdateTxBody.getKey());
//		  }
//
//		  if (crUpdateTxBody.getProxyAccountID() != null) {
//				cryptoAcctUpdateBodySize += (3 * LONG_SIZE);
//		  }
//
//		  if (crUpdateTxBody.getProxyFraction() != 0) {
//				cryptoAcctUpdateBodySize += INT_SIZE;
//		  }
//
//		  if (crUpdateTxBody.getSendRecordThreshold() != 0) {
//				cryptoAcctUpdateBodySize += LONG_SIZE;
//		  }
//
//		  if (crUpdateTxBody.getReceiveRecordThreshold() != 0) {
//				cryptoAcctUpdateBodySize += LONG_SIZE;
//		  }
//
//		  if (crUpdateTxBody.getAutoRenewPeriod() != null) {
//				cryptoAcctUpdateBodySize += (LONG_SIZE + INT_SIZE);
//		  }
//
//		  if (crUpdateTxBody.getExpirationTime() != null) {
//				cryptoAcctUpdateBodySize += (LONG_SIZE + INT_SIZE);
//		  }
//
//
//		  return cryptoAcctUpdateBodySize;
//
//	 }

	 /**
	  * This method calculates total total RAM Bytes (product of total bytes that will be stored in memory and  time till account expires)
	  *
	  * @param body
	  * @return
	  */
	 private long getCryptoRBS(TransactionBody body) {
		  // Number of bytes stored in memory
		  /*
			* AccountID => 3 long values - 24 bytes
			* long balance => 8 bytes
			* long receiverThreshold => 8 bytes
			* long senderThreshold => 8 bytes
			* boolean receiverSigRequired => 4 bytes
			* String solidityAddress => 20 bytes
			* Key => determined by number of keys and threshold values => key size (32 bytes) ,threshold (4 bytes)
			*/

		  long rbsSize = 0;
		  if (body.getCryptoCreateAccount() != null) {
				CryptoCreateTransactionBody cryptoCreate = body.getCryptoCreateAccount();
				long seconds = Math.round(cryptoCreate.getAutoRenewPeriod().getSeconds());
				// size for account bytes stored in memory
				// size = AccountID + balance + receiverThreshold + senderThreshold +
				// receiverSigRequired + solidityAddress + (accountKeys + threshold)
				rbsSize = (6 * LONG_SIZE + BOOL_SIZE + SOLIDITY_ADDRESS + getAccountKeyStorageSize(cryptoCreate.getKey())) * seconds;
		  }
		  return rbsSize;
	 }

	 /**
	  * This method returns the total bytes in Crypto Transaction body
	  *
	  * @param body
	  * @return
	  */
	 private int getCryptoCreateAccountBodyTxSize(TransactionBody body) {
		/*
		 *  Key key - calculated value
		    uint64 initialBalance - LONG_SIZE
		    AccountID proxyAccountID - 3 * LONG_SIZE
		    int32 proxyFraction  - INT_SIZE
		    int32 maxReceiveProxyFraction - INT_SIZE
		    uint64 sendRecordThreshold - LONG_SIZE
		    uint64 receiveRecordThreshold  - LONG_SIZE
		    bool receiverSigRequired - BOOL_SIZE
		    Duration autoRenewPeriod - (LONG_SIZE + INT_SIZE)		
		    ShardID shardID - LONG_SIZE
		    RealmID realmID - LONG_SIZE
		    Key newRealmAdminKey - calculated value
		 */

		  int keySize = getAccountKeyStorageSize(body.getCryptoCreateAccount().getKey());
		  int newRealmAdminKeySize = 0;
		  if (body.getCryptoCreateAccount().hasNewRealmAdminKey()) {
				newRealmAdminKeySize = getAccountKeyStorageSize(body.getCryptoCreateAccount().getNewRealmAdminKey());
		  }

		  int cryptoAcctBodySize = keySize + LONG_SIZE + (3 * LONG_SIZE) + INT_SIZE + INT_SIZE + LONG_SIZE + LONG_SIZE + BOOL_SIZE + (LONG_SIZE + INT_SIZE) + LONG_SIZE + LONG_SIZE + newRealmAdminKeySize;

		  return cryptoAcctBodySize;

	 }

	 /**
	  * This method calculates total total Storage Bytes (product of total bytes that will be stored in File Storage and  time till account expires)
	  *
	  * @param body
	  * @return
	  */
	 private long getCryptoCreateStorageBytesSec(TransactionBody body) {

		  /*
			* AccountID proxyAccountID  - 24 bytes
			* int32 proxyFraction  - 4 bytes
			* int32 maxReceiveProxyFraction - 4 bytes
			* Duration autoRenewPeriod - 12 bytes (long + int)
			* Key newRealmAdminKey - get the size from Key object
			*
			*/
		  // size = AccountID (3 * long) + proxyFraction (int) + maxReceiveProxyFraction (int) + autoRenewPeriod (long + int) + newRealmAdminKey (key)

		  int newRealmAdminKeySize = 0;
		  if (body.getCryptoCreateAccount().hasNewRealmAdminKey()) {
				newRealmAdminKeySize = getAccountKeyStorageSize(body.getCryptoCreateAccount().getNewRealmAdminKey());
		  }
		  long storageSize = 3 * LONG_SIZE + INT_SIZE + INT_SIZE + LONG_SIZE + INT_SIZE + newRealmAdminKeySize;
		  long seconds = Math.round(body.getCryptoCreateAccount().getAutoRenewPeriod().getSeconds());
		  storageSize = storageSize * seconds;
		  return storageSize;

	 }


	 private int getCryptoTransferBodyTxSize(TransactionBody body) {
		
		
		/*
		 * TransferList transfers
		 		repeated AccountAmount
		 					AccountID - (3 * LONG_SIZE)
		 					sint64 amount - LONG_SIZE
		 */

		  int accountAmountCount = body.getCryptoTransfer().getTransfers().getAccountAmountsCount();
		  int cryptoTransfertBodySize = ((3 * LONG_SIZE) + LONG_SIZE) * accountAmountCount;
		  return cryptoTransfertBodySize;

	 }

	 private long getTV(TransactionBody body) {
		  long amount = 0;
		  TransferList transferList = body.getCryptoTransfer().getTransfers();
		  List<AccountAmount> accountAmounts = transferList.getAccountAmountsList();
		  for (AccountAmount actAmt : accountAmounts) {
				if (actAmt.getAmount() > 0) {
					 amount = amount + actAmt.getAmount();
				}
		  }

		  return Math.round(amount / 1000);
	 }

	 ////////////////////////////////////////////////////////////////////////// Query Fee ////////////////////////////////////////////////////////////

	 /**
	  * This method returns the Fee Matrices for balance query
	  *
	  * @return
	  */
	 public FeeComponents getBalanceQueryFeeMatrices() {
		  // get the Fee Matrices
		  long bpt = 0;
		  long vpt = 0;
		  long rbs = 0;
		  long sbs = 0;
		  long gas = 0;
		  long tv = 0;
		  long bpr = 0;
		  long sbpr = 0;


		  /*
			*  CryptoGetAccountBalanceQuery
			*  		QueryHeader
			*  			Transaction - CryptoTransfer - (will be taken care in Transaction processing)
			*  			ResponseType - INT_SIZE
			*  		AccountID -  3 * LONG_SIZE
			*/

		  bpt = INT_SIZE + (3 * LONG_SIZE);

		  /*
			* CryptoGetAccountBalanceResponse
			* Response header
			* NodeTransactionPrecheckCode -  4 bytes
			* ResponseType - 4 bytes
			* AccountID - 24 bytes (consist of 3 long values)
			* balance - 8 bytes (1 long value)
			*/

		  bpr = INT_SIZE + INT_SIZE + 3 * LONG_SIZE + LONG_SIZE;

		  /*
			* Account Balance Storage Size
			*
			* AccountID - 24 bytes (consist of 3 long values) balance - 8 bytes (1 long
			* value)
			*/

		  sbpr = 3 * LONG_SIZE + LONG_SIZE;

		  FeeComponents feeMatrix = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs).setSbs(sbs).setGas(gas)
					 .setTv(tv).setBpr(bpr).setSbpr(sbpr).build();

		  return feeMatrix;
	 }

	 /**
	  * This method returns the Fee Matrices for query (for getting the cost of Transaction Record Query)
	  *
	  * @return
	  */
	 public FeeComponents getCostTransactionRecordQueryFeeMatrices() {
		  // get the Fee Matrices
		  long bpt = 0;
		  long vpt = 0;
		  long rbs = 0;
		  long sbs = 0;
		  long gas = 0;
		  long tv = 0;
		  long bpr = 0;
		  long sbpr = 0;
		
		/*
		 *  CostTransactionGetRecordQuery
		 *  		QueryHeader
		 *  			Transaction - CryptoTransfer - (will be taken care in Transaction processing)
		 *  			ResponseType - INT_SIZE
		 *  		TransactionID 
		 *  			AccountID accountID  - 3 * LONG_SIZE bytes
    					Timestamp transactionValidStart - (LONG_SIZE + INT_SIZE) bytes
		 */

		  bpt = INT_SIZE + (3 * LONG_SIZE) + (LONG_SIZE + INT_SIZE);

		  /*
			* Response header
			* NodeTransactionPrecheckCode -  4 bytes
			* ResponseType - 4 bytes
			* uint64 cost - 8 bytes
			*/
		  bpr = INT_SIZE + INT_SIZE + LONG_SIZE;


		  FeeComponents feeMatrix = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs).setSbs(sbs).setGas(gas)
					 .setTv(tv).setBpr(bpr).setSbpr(sbpr).build();

		  return feeMatrix;
	 }


	 /**
	  * This method returns the Fee matrices for Transaction Record query
	  *
	  * @param transRecord
	  * @return
	  */
	 public FeeComponents getTransactionRecordQueryFeeMatrices(TransactionRecord transRecord) {

		  if (transRecord == null) return getDefaultMatrices();
		  // get the Fee Matrices
		  long bpt = 0;
		  long vpt = 0;
		  long rbs = 0;
		  long sbs = 0;
		  long gas = 0;
		  long tv = 0;
		  long bpr = 0;
		  long sbpr = 0;
		
		/*
		 *  TransactionGetRecordQuery
		 *  		QueryHeader
		 *  			Transaction - CryptoTransfer - (will be taken care in Transaction processing)
		 *  			ResponseType - INT_SIZE
		 *  		TransactionID 
		 *  			AccountID accountID  - 3 * LONG_SIZE bytes
    					Timestamp transactionValidStart - (LONG_SIZE + INT_SIZE) bytes
		 */

		  bpt = INT_SIZE + (3 * LONG_SIZE) + (LONG_SIZE + INT_SIZE);

		  /*
			* bpr = TransactionRecordResponse Response header NodeTransactionPrecheckCode -
			* 4 bytes ResponseType - 4 bytes Transaction Record Size
			*
			*/
		  int txRecordSize = getAccountTransactionRecordSize(transRecord);

		  bpr = INT_SIZE + INT_SIZE + txRecordSize;

		  /*
			* sbpr = Transaction Record Size (no other file/data is required to create this
			* response)
			*
			*/

		  sbpr = txRecordSize;

		  FeeComponents feeMatrix = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs).setSbs(sbs).setGas(gas)
					 .setTv(tv).setBpr(bpr).setSbpr(sbpr).build();

		  return feeMatrix;
	 }

	 /**
	  * This method returns the Fee matrices for Account Info query
	  *
	  * @param key
	  * @param claimList
	  * @return
	  */
	 public FeeComponents getAccountInfoQueryFeeMatrices(Key key, List<Claim> claimList) {
		  // get the Fee Matrices
		  long bpt = 0;
		  long vpt = 0;
		  long rbs = 0;
		  long sbs = 0;
		  long gas = 0;
		  long tv = 0;
		  long bpr = 0;
		  long sbpr = 0;

		  /*
			*  CryptoGetInfoQuery
			*  		QueryHeader
			*  			Transaction - CryptoTransfer - (will be taken care in Transaction processing)
			*  			ResponseType - INT_SIZE
			*  		AccountID accountID  - 3 * LONG_SIZE bytes

			*/

		  bpt = INT_SIZE + (3 * LONG_SIZE);

		  /*
			* bpr =
			* CryptoGetInfoResponse
			* Response header NodeTransactionPrecheckCode -  4 bytes
			* ResponseType - 4 bytes
			* AccountInfo accountInfo - calculated value
			*
			*/
		  int accountInfoSize = getAccountInfoSize(key, claimList);

		  bpr = INT_SIZE + INT_SIZE + accountInfoSize;

		  /*
			* sbpr = Transaction Record Size (no other file/data is required to create this  response)
			*
			*/

		  sbpr = accountInfoSize;

		  FeeComponents feeMatrix = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs).setSbs(sbs).setGas(gas)
					 .setTv(tv).setBpr(bpr).setSbpr(sbpr).build();

		  return feeMatrix;
	 }

	 /**
	  * This method returns the Fee Matrices for Account Records query
	  *
	  * @param transRecord
	  * @return
	  */
	 public FeeComponents getCryptoAccountRecordsQueryFeeMatrices(List<TransactionRecord> transRecord) {
		  // get the Fee Matrices
		  long bpt = 0;
		  long vpt = 0;
		  long rbs = 0;
		  long sbs = 0;
		  long gas = 0;
		  long tv = 0;
		  long bpr = 0;
		  long sbpr = 0;

		  /*
			*  CryptoGetAccountRecordsQuery
			*  		QueryHeader
			*  			Transaction - CryptoTransfer - (will be taken care in Transaction processing)
			*  			ResponseType - INT_SIZE
			*  		AccountID  - 3 * LONG_SIZE
			*
			*/

		  bpt = INT_SIZE + (3 * LONG_SIZE);

		  /*
			* bpr = TransactionRecordResponse Response header NodeTransactionPrecheckCode -
			* 4 bytes ResponseType - 4 bytes AccountID accountID - 24 bytes repeated
			* TransactionRecord - get size from records
			*
			*/
		  int txRecordListsize = 0;
		  for (TransactionRecord record : transRecord) {
				txRecordListsize = txRecordListsize + getAccountTransactionRecordSize(record);
		  }

		  bpr = INT_SIZE + INT_SIZE + txRecordListsize;

		  /*
			* sbpr = TransactionRecordList Size (no other file/data is required to create this response)
			*
			*/

		  sbpr = txRecordListsize;

		  FeeComponents feeMatrix = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs).setSbs(sbs).setGas(gas)
					 .setTv(tv).setBpr(bpr).setSbpr(sbpr).build();

		  return feeMatrix;
	 }

	 /**
	  * This method returns the Fee Matrices for query (for getting the cost of Account Record Query)
	  *
	  * @return
	  */
	 public FeeComponents getCostCryptoAccountRecordsQueryFeeMatrices() {

		  return getCostForQueryByIDOnly();
	 }

	 /**
	  * This method returns the Fee Matrices for query (for getting the cost of Account Info Query)
	  *
	  * @return
	  */
	 public FeeComponents getCostCryptoAccountInfoQueryFeeMatrices() {

		  return getCostForQueryByIDOnly();
	 }


	 private int getAccountInfoSize(Key accountKey, List<Claim> claimList) {

		/*
		 *  AccountID accountID  - 3 * LONG_SIZE
	        string contractAccountID - SOLIDITY_ADDRESS
	        bool deleted - BOOL_SIZE
	        AccountID proxyAccountID - 3 * LONG_SIZE
	        int32 proxyFraction - INT_SIZE
	        int64 proxyReceived - INT_SIZE
	        Key key - calculated value
	        uint64 balance - LONG_SIZE
	        uint64 generateSendRecordThreshold - LONG_SIZE
	        uint64 generateReceiveRecordThreshold - LONG_SIZE
	        bool receiverSigRequired - BOOL_SIZE
	        Timestamp expirationTime - LONG_SIZE + INT_SIZE
	        Duration autoRenewPeriod - LONG_SIZE + INT_SIZE
	        repeated Claim claims - calculated value
	        		AccountID accountID - 3 * LONG_SIZE
				    bytes hash - 48 byte SHA-384 hash (presumably of some kind of credential or certificate)
				    KeyList keys - calculated value
		 * 
		 */


		  int keySize = getAccountKeyStorageSize(accountKey);

		  int claimsKeySize = 0;
		  int claimHashSize = 0;
		  int claimsAccountID = 0;

		  if (claimList != null) {
				int claimsListSize = claimList.size();
				claimHashSize = TX_HASH_SIXE * claimsListSize;
				claimsAccountID = (3 * LONG_SIZE) * claimsListSize;

				for (Claim claims : claimList) {
					 List<Key> keyList = claims.getKeys().getKeysList();
					 for (Key key : keyList) {
						  claimsKeySize += getAccountKeyStorageSize(key);
					 }
				}
		  }

		  int accountInfoSize = (3 * LONG_SIZE) + SOLIDITY_ADDRESS + BOOL_SIZE + (3 * LONG_SIZE) + INT_SIZE + LONG_SIZE + keySize + LONG_SIZE + LONG_SIZE + LONG_SIZE + BOOL_SIZE +
					 (LONG_SIZE + INT_SIZE) + (LONG_SIZE + INT_SIZE) + claimsAccountID + claimHashSize + claimsKeySize;


		  return accountInfoSize;

	 }

	 private int getAccountTransactionRecordSize(TransactionRecord transRecord) {

		  /*
			* TransactionReceipt - 4 bytes + 3 * LONG_SIZE
			* bytes transactionHash - 96 bytes Timestamp
			* consensusTimestamp - 8 bytes + 4 bytes
			* TransactionID - 32 bytes (AccountID - 24 +  Timestamp - 8)
			* string memo - get from the record
			* uint64 transactionFee - 8
			* bytes TransferList transferList - get from actual transaction record
			*
			*/

		  int totalHashBytes = 0;
		  if (transRecord.getTransactionHash() != null) {
				totalHashBytes = TX_HASH_SIXE;
		  }
		  int memoBytesSize = 0;
		  if (transRecord.getMemo() != null) {
				memoBytesSize = transRecord.getMemo().getBytes().length;
		  }

		  int acountAmountSize = 0;
		  if (transRecord.getTransferList() != null) {
				int accountAmountCount = transRecord.getTransferList().getAccountAmountsCount();
				acountAmountSize = accountAmountCount * (LONG_SIZE + INT_SIZE); // (24 bytes AccountID and 8 bytes Amount)
		  }

		  int txRecordSize = INT_SIZE + (3 * LONG_SIZE) + totalHashBytes + (LONG_SIZE + INT_SIZE) + (3 * LONG_SIZE + LONG_SIZE + INT_SIZE) + memoBytesSize + LONG_SIZE + acountAmountSize;

		  return txRecordSize;

	 }
	
	/*private long getCryptoClaimStorageBytesSec(Transaction tx) {

		// Get the
		long storageSize = tx.getBody().getCryptoAddClaim().getClaim().getHash().size()
				+ tx.getBody().getCryptoAddClaim().getClaim().getKeys().getSerializedSize();

		// get the expiration date

		AccountID acctID = tx.getBody().getCryptoAddClaim().getAccountID();

		long accountNum = acctID.getAccountNum();
		long realmNum = acctID.getRealmNum();
		long shardNum = acctID.getShardNum();	

		// getAccountKeyStorageSize();
		long seconds = Math.round(tx.getBody().getCryptoCreateAccount().getAutoRenewPeriod().getSeconds()
				+ (tx.getBody().getCryptoCreateAccount().getAutoRenewPeriod().getNanos() / 1000000000));
		storageSize = storageSize * seconds;
		return storageSize;

	}*/


}
