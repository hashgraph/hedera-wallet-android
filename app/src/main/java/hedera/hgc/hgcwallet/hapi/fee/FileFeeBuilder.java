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
 * This class includes methods for generating Fee Matrices and calculating Fee for File related
 * Transactions and Query.
 */
public class FileFeeBuilder extends FeeBuilder {

  /**
   * This method returns Fee Matrices for File Info Query
   */
//  public FeeComponents getFileInfoQueryFeeMatrices(KeyList keys) {
//
//    // get the Fee Matrices
//    long bpt = 0;
//    long vpt = 0;
//    long rbs = 0;
//    long sbs = 0;
//    long gas = 0;
//    long tv = 0;
//    long bpr = 0;
//    long sbpr = 0;
//
//
//    /*
//     * FileGetContentsQuery QueryHeader Transaction - CryptoTransfer - (will be taken care in
//     * Transaction processing) ResponseType - INT_SIZE FileID - 3 * LONG_SIZE
//     */
//
//    bpt = INT_SIZE + (3 * LONG_SIZE);
//    /*
//     *
//     * Response header NodeTransactionPrecheckCode - 4 bytes ResponseType - 4 bytes
//     *
//     * FileInfo FileID fileID - 3 * LONG_SIZE int64 size - LONG_SIZE Timestamp expirationTime = 3;
//     * // the current time at which this account is set to expire bool deleted = 4; // true if
//     * deleted but not yet expired KeyList keys = 5; // one of these keys must sign in order to
//     * modify or delete the file
//     *
//     */
//    int keySize = 0;
//    if (keys != null) {
//      List<Key> waclKeys = keys.getKeysList();
//      for (Key key : waclKeys) {
//        keySize += getAccountKeyStorageSize(key);
//      }
//    }
//
//    bpr = INT_SIZE + INT_SIZE + 3 * LONG_SIZE + LONG_SIZE + (LONG_SIZE) + BOOL_SIZE + keySize;
//
//    sbpr = 3 * LONG_SIZE + LONG_SIZE + (LONG_SIZE) + BOOL_SIZE + keySize;
//    ;
//
//    FeeComponents feeMatrix = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs)
//        .setSbs(sbs).setGas(gas).setTv(tv).setBpr(bpr).setSbpr(sbpr).build();
//
//    return feeMatrix;
//
//  }
//
//
//  /**
//   * This method returns Fee Matrices for File Content Query
//   */
//  public FeeComponents getFileContentQueryFeeMatrices(int contentSize) {
//
//    // get the Fee Matrices
//    long bpt = 0;
//    long vpt = 0;
//    long rbs = 0;
//    long sbs = 0;
//    long gas = 0;
//    long tv = 0;
//    long bpr = 0;
//    long sbpr = 0;
//
//
//    /*
//     * FileGetContentsQuery QueryHeader Transaction - CryptoTransfer - (will be taken care in
//     * Transaction processing) ResponseType - INT_SIZE FileID - 3 * LONG_SIZE
//     */
//
//    bpt = INT_SIZE + (3 * LONG_SIZE);
//    /*
//     *
//     * Response header NodeTransactionPrecheckCode - 4 bytes ResponseType - 4 bytes
//     *
//     * FileContents FileID fileID - 3 * LONG_SIZE bytes content - calculated value (size of the
//     * content)
//     *
//     */
//
//    bpr = INT_SIZE + INT_SIZE + 3 * LONG_SIZE + contentSize;
//
//    sbpr = 3 * LONG_SIZE + contentSize;
//
//    FeeComponents feeMatrix = FeeComponents.newBuilder().setBpt(bpt).setVpt(vpt).setRbs(rbs)
//        .setSbs(sbs).setGas(gas).setTv(tv).setBpr(bpr).setSbpr(sbpr).build();
//
//    return feeMatrix;
//
//  }
//
//
//  /**
//   * This method returns total bytes in File Create Transaction
//   */
//  private int getFileCreateTxSize(TransactionBody txBody) {
//    /*
//     * Timestamp expirationTime - (LONG_SIZE + INT_SIZE) KeyList keys - calculated value bytes
//     * contents -get the size ShardID shardID - LONG_SIZE RealmID realmID - LONG_SIZE Key
//     * newRealmAdminKey - calculated value
//     */
//
//    FileCreateTransactionBody fileCreateTxBody = txBody.getFileCreate();
//    List<Key> waclKeys = fileCreateTxBody.getKeys().getKeysList();
//
//    int keySize = 0;
//
//    for (Key key : waclKeys) {
//      keySize += getAccountKeyStorageSize(key);
//    }
//    int newRealmAdminKeySize = 0;
//
//    if (fileCreateTxBody.getNewRealmAdminKey() != null) {
//      newRealmAdminKeySize = getAccountKeyStorageSize(fileCreateTxBody.getNewRealmAdminKey());
//    }
//    int fileContentsSize = 0;
//    if (fileCreateTxBody.getContents() != null) {
//      fileContentsSize = fileCreateTxBody.getContents().size();
//    }
//
//    int cryptoFileCreateSize = (LONG_SIZE + INT_SIZE) + keySize + fileContentsSize + (3 * LONG_SIZE)
//        + newRealmAdminKeySize;
//
//    return cryptoFileCreateSize;
//
//  }
//
//  /**
//   * This method returns total bytes in File Update Transaction Body
//   */
//  private int getFileUpdateBodyTxSize(TransactionBody txBody) {
//    /*
//     * FileID fileID = 1; // the file to update Timestamp expirationTime = 2; // the new time at
//     * which it should expire (ignored if not later than the current value) KeyList keys = 3; // the
//     * keys that can modify or delete the file bytes contents = 4; // the new file contents. All the
//     * bytes in the old contents are discarded.
//     */
//
//    int fileUpdateBodySize = 3 * LONG_SIZE;
//    FileUpdateTransactionBody fileUpdateTxBody = txBody.getFileUpdate();
//
//    if (fileUpdateTxBody.getKeys() != null) {
//      List<Key> waclKeys = fileUpdateTxBody.getKeys().getKeysList();
//      int keySize = 0;
//      for (Key key : waclKeys) {
//        keySize = +getAccountKeyStorageSize(key);
//      }
//      fileUpdateBodySize = +keySize;
//    }
//
//    if (fileUpdateTxBody.getExpirationTime() != null) {
//      fileUpdateBodySize += (LONG_SIZE + INT_SIZE);
//    }
//
//    if (fileUpdateTxBody.getContents() != null) {
//      fileUpdateBodySize += fileUpdateTxBody.getContents().size();
//    }
//
//    return fileUpdateBodySize;
//
//  }


}
