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

package hedera.hgc.hgcwallet.hapi.fee

object FeeCalc {
//    fun getFeeMap(): Map<HederaFunctionality, FeeData>? {
//        try {
//            val inputStream = App.instance.assets.open("feeScheduleproto.txt")
//            val size = inputStream.available()
//            val buffer = ByteArray(size)
//            inputStream.read(buffer)
//            inputStream.close()
//
//            // Get the FeeSchedule Object
//            val feeSch = FeeSchedule.parseFrom(buffer)
//            val feeSchMap = HashMap<HederaFunctionality, FeeData>()
//            val transFeeSchList = feeSch.transactionFeeScheduleList
//
//            for (transSch in transFeeSchList) {
//                feeSchMap[transSch.hederaFunctionality] = transSch.feeData
//            }
//
//            return feeSchMap
//
//        } catch (ex: IOException) {
//            ex.printStackTrace()
//            return null
//        }
//    }
//
//    fun feeForTransferTransaction(txn: Transaction): Long {
//        return getFeeMap()?.let {
//            val feeData = it[HederaFunctionality.CryptoTransfer]
//            CryptoFeeBuilder().run {
//                // get the FeeMatrices for specific functionlaity
//                val feeMatrics = getCryptoTransferTxFeeMatrices(txn)
//                // pass the FeeData and generated Matrices and get the Fee
//                getTotalFeeforRequest(feeData, feeMatrics)
//            }
//        } ?: 0
//    }
//
//    fun feeForGetBalance(): Long {
//        // get the FeeData for specific functionality from map
//        return getFeeMap()?.let {
//            val feeData = it[HederaFunctionality.CryptoGetAccountBalance]
//            CryptoFeeBuilder().run {
//                // get the FeeMatrices for specific functionlaity
//                val feeMatrics = balanceQueryFeeMatrices
//                // pass the FeeData and generated Matrices and get the Fee
//                getTotalFeeforRequest(feeData, feeMatrics)
//            }
//        } ?: 0
//    }
//
//    fun feeForGetAccountRecordCostQuery(): Long {
//        // get the FeeData for specific functionality from map
//        return getFeeMap()?.let {
//            val feeData = it[HederaFunctionality.CryptoGetAccountRecords]
//            CryptoFeeBuilder().run {
//                // get the FeeMatrices for specific functionlaity
//                val feeMatrics = costCryptoAccountRecordsQueryFeeMatrices
//                // pass the FeeData and generated Matrices and get the Fee
//                getTotalFeeforRequest(feeData, feeMatrics)
//            }
//
//
//        } ?: 0
//
//    }


}