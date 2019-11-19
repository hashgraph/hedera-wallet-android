/*
 *
 *  Copyright 2019 Hedera Hashgraph LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package hedera.hgc.hgcwallet.hapi.fee;

/**
 * This is the base class for building Fee Matrices and calculating the Total as well as specific component Fee 
 * for a given Transaction or Query. It includes common methods which is used to calculate Fee for Crypto, File and 
 * Smart Contracts Transactions and Query
*/


public class FeeBuilder {

	public static final int LONG_SIZE = 8;
	public static final int INT_SIZE = 4;
	public static final int BOOL_SIZE = 4;
	public static final int SOLIDITY_ADDRESS = 20;
	public static final int KEY_SIZE = 32;
	public static final int TX_HASH_SIXE = 48;

	
	/**
	 * This method calculates Fee for specific component (Noe/Network/Service) based upon param componentCoefficients and componentMetrics
	 * @param componentCoefficients
	 * @param componentMetrics
	 * @return
	 */
//	public long getComponentFee(FeeComponents componentCoefficients, FeeComponents componentMetrics) {
//
//		long bytesUsageFee = componentCoefficients.getBpt() * componentMetrics.getBpt();
//		long verificationFee = componentCoefficients.getVpt() * componentMetrics.getVpt();
//		long ramStorageFee = componentCoefficients.getRbs() * componentMetrics.getRbs();
//		long storageFee = componentCoefficients.getSbs() * componentMetrics.getSbs();
//		long evmGasFee = componentCoefficients.getGas() * componentMetrics.getGas();
//		long txValueFee = Math.round((componentCoefficients.getTv() * componentMetrics.getTv()) / 1000);
//		long bytesResponseFee = componentCoefficients.getBpr() * componentMetrics.getBpr();
//		long storageBytesResponseFee = componentCoefficients.getSbpr() * componentMetrics.getSbs();
//
//		long totalComponentFee = componentCoefficients.getConstant() +
//								(bytesUsageFee + verificationFee + ramStorageFee + storageFee + evmGasFee + txValueFee + bytesResponseFee +  storageBytesResponseFee);
//
//		if(totalComponentFee < componentCoefficients.getMin()) {
//			totalComponentFee = componentCoefficients.getMin();
//		}else if(totalComponentFee > componentCoefficients.getMax()) {
//			totalComponentFee = componentCoefficients.getMax();
//		}
//		return totalComponentFee;
//	}
//
//	/**
//	 * This method calculates Total Fee for Transaction or Query.
//	 * @param feeCoefficients
//	 * @param componentMetrics
//	 * @return
//	 */
//	public long getTotalFeeforRequest(FeeData feeCoefficients , FeeComponents componentMetrics ) {
//
//		// get Node Fee
//		FeeComponents nodeFeeCoefficients = feeCoefficients.getNodedata();
//		long  nodeFee = getComponentFee(nodeFeeCoefficients,componentMetrics);
//
//	//	System.out.println("The Node Fee is "+nodeFee);
//		// get Network fee
//		FeeComponents networkFeeCoefficients = feeCoefficients.getNetworkdata();
//		long  networkFee = getComponentFee(networkFeeCoefficients,componentMetrics);
//	//	System.out.println("The networkFee Fee is "+networkFee);
//		// get Service Fee
//		FeeComponents serviceFeeCoefficients = feeCoefficients.getServicedata();
//		long  serviceFee = getComponentFee(serviceFeeCoefficients,componentMetrics);
//	//	System.out.println("The serviceFee Fee is "+serviceFee);
//
//		long totalFee = nodeFee + networkFee + serviceFee;
//		return totalFee;
//	}
//
//
//	/**
//	 * This method calculates the common bytes included in a every transaction. Common bytes only differ based upon memo field.
//	 * @param tx
//	 * @return
//	 */
//	public int getCommonTransactionBodyBytes(Transaction tx) {
//		/*
//		 * Common fields in all transaction
//
//		 *  TransactionID transactionID
//		   		AccountID accountID  - 3 * LONG_SIZE bytes
//    			Timestamp transactionValidStart - (LONG_SIZE + INT_SIZE) bytes
//		    AccountID nodeAccountID  - 3 * LONG_SIZE bytes
//		    uint64 transactionFee  - LONG_SIZE bytes
//		    Duration transactionValidDuration - (LONG_SIZE + INT_SIZE) bytes
//		    bool generateRecord  - BOOL_SIZE bytes
//		    string memo  - get memo size from transaction
//		 *
//		 */
//
//		int commonTxBytes = 3 * LONG_SIZE + (LONG_SIZE + INT_SIZE) + 3 * LONG_SIZE + LONG_SIZE + (LONG_SIZE + INT_SIZE) + BOOL_SIZE + APIRequestBuilder.INSTANCE.getTxnBody(tx).getMemo().getBytes().length;
//		return commonTxBytes;
//
//	}
//
//	/**
//	 * This method is invoked by individual Fee builder classes to calculated the number of signatures in transaction.
//	 * @param tx
//	 * @return
//	 */
//	public long getVPT(Transaction tx) {
//		// need to verify recursive depth of signatures
//		if (tx == null) return 0;
//		Signature sig = Signature.newBuilder().setSignatureList(tx.getSigs()).build();
//		return calculateNoOfSigs(sig, 0);
//	}
//
//	/**
//	 * This method returns the gas converted to hashbar units. (This needs to be updated)
//	 * @param tx
//	 * @return
//	 */
//	public long getGas(Transaction tx) {
//		long gas = 0;
//		TransactionBody body = APIRequestBuilder.INSTANCE.getTxnBody(tx);
//		if (body.getContractCreateInstance() != null) {
//			gas = body.getContractCreateInstance().getGas();
//		} else if (body.getContractCall() != null) {
//			gas = body.getContractCall().getGas();
//		}
//		return gas * 1; // 1 Gas = 1 hashbars - need to get from standrd configuration
//	}
//
//	/**
//	 * This method returns the Key size in bytes
//	 * @param key
//	 * @return
//	 */
//	public int getAccountKeyStorageSize(Key key) {
//
//		int keyStorageSize  =0;
//		try {
//		int[] countKeyMetatData = { 0, 0 };
//		countKeyMetatData = calculateKeysMetadata(key, countKeyMetatData);
//		keyStorageSize = countKeyMetatData[0] * KEY_SIZE + countKeyMetatData[1] * INT_SIZE;
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//		return keyStorageSize;
//	}
//
//
//	/**
//	 * This method calculates number of signature in Signature object
//	 * @param sig
//	 * @param count
//	 * @return
//	 */
//	private int calculateNoOfSigs(Signature sig, int count) {
//		if (sig.getSignatureList() != null) {
//			List<Signature> sigList = sig.getSignatureList().getSigsList();
//			for (int i = 0; i < sigList.size(); i++) {
//				count = calculateNoOfSigs(sigList.get(i), count);
//			}
//		} else if (sig.getThresholdSignature() != null) {
//			List<Signature> sigList = sig.getThresholdSignature().getSigs().getSigsList();
//			for (int i = 0; i < sigList.size(); i++) {
//				count = calculateNoOfSigs(sigList.get(i), count);
//			}
//		} else {
//			count++;
//		}
//		return count;
//	}
//
//	/**
//	 * This method calculates number of keys
//	 * @param key
//	 * @param count
//	 * @return
//	 */
//	private static int[] calculateKeysMetadata(Key key, int[] count) {
//		if (key.getKeyList() != null) {
//			List<Key> keyList = key.getKeyList().getKeysList();
//			count[1]++;
//			for (int i = 0; i < keyList.size(); i++) {
//				count = calculateKeysMetadata(keyList.get(i), count);
//			}
//		} else if (key.getThresholdKey() != null) {
//			List<Key> keyList = key.getThresholdKey().getKeys().getKeysList();
//			count[1]++;
//			for (int i = 0; i < keyList.size(); i++) {
//				count = calculateKeysMetadata(keyList.get(i), count);
//			}
//		} else {
//			count[0]++;
//		}
//		return count;
//	}
//
//	/**
//	 * This method returns the Fee Matrices for querying based upon ID (Account / File / Smart Contract)
//	 * @return
//	 */
//	public FeeComponents getCostForQueryByIDOnly() {
//
//		// get the Fee Matrices
//				long bpt = 0;
//				long vpt = 0;
//				long rbs = 0;
//				long sbs = 0;
//				long gas = 0;
//				long tv = 0;
//				long bpr = 0;
//				long sbpr = 0;
//
//				/*
//				 *  	Query
//				 *  		QueryHeader
//				 *  			Transaction - CryptoTransfer - (will be taken care in Transaction processing)
//				 *  			ResponseType - INT_SIZE
//				 *  		ID  - 3 * LONG_SIZE
//				 *
//				 */
//
//				bpt = INT_SIZE + (3 * LONG_SIZE);
//
//				/*
//				 * bpr =
//				 *  Response header
//				 *  NodeTransactionPrecheckCode -  4 bytes
//				 *  ResponseType - 4 bytes
//				 *  uint64 cost - 8 bytes
//				 */
//
//
//				bpr = INT_SIZE + INT_SIZE + LONG_SIZE;
//
//
//				FeeComponents feeMatrix = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs).setSbs(sbs).setGas(gas)
//						.setTv(tv).setBpr(bpr).setSbpr(sbpr).build();
//
//				return feeMatrix;
//
//	}
//
//	/**
//	 * It returns the default Fee Matrices
//	 * @return
//	 */
//	public FeeComponents getDefaultMatrices() {
//			FeeComponents feeMatricesForTx = FeeComponents.newBuilder().setBpt(0).setVpt(0).setRbs(0).setSbs(0)
//					  .setGas(0).setTv(0).setBpr(0).setSbpr(0)
//					  .build();
//			return feeMatricesForTx;
//	}


}
