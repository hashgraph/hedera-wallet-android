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

package hedera.hgc.hgcwallet.ui.main.transcation


import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.contact.Contact
import hedera.hgc.hgcwallet.database.transaction.TxnRecord
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class TransactionDetailsScreen(var txnRecord: TxnRecord) : Screen<TransactionDetailsView>() {

    override fun createView(context: Context): TransactionDetailsView {
        return TransactionDetailsView(context, txnRecord)
    }

    internal fun onCloseClick() {
        navigator?.goBack()
    }

    internal fun copyAccount(accountID: String) {
        Singleton.copyToClipBoard(accountID, activity)
        Singleton.showToast(activity, activity.getString(R.string.copy_data_clipboard_account_id))
    }
}

class TransactionDetailsView(context: Context, val txnRecord: TxnRecord) : BaseScreenView<TransactionDetailsScreen>(context) {

    private val mTextViewHgcAmount: TextView
    private val mTextViewDollarAmount: TextView
    private val mTextViewTime: TextView
    private val mTextViewFromAccountName: TextView
    private val mTextViewFromAccountId: TextView
    private val mTextViewHgcIcon: TextView
    private val mTextViewToAccountName: TextView
    private val mTextViewToAccountId: TextView
    private val mTextViewFeeValue: TextView
    private val mEditTextNote: EditText
    private val mImageViewCopyFromId: ImageView
    private val mImageViewCopyToId: ImageView

    init {
        View.inflate(context, R.layout.view_transaction_details_layout, this)

        mTextViewHgcAmount = findViewById<View>(R.id.text_hgc_amount) as TextView
        mTextViewDollarAmount = findViewById<View>(R.id.text_dollor_amount) as TextView
        mTextViewTime = findViewById<View>(R.id.text_time) as TextView
        mTextViewFromAccountName = findViewById<View>(R.id.text_from_account_name) as TextView
        mTextViewFromAccountId = findViewById<View>(R.id.text_from_account_id) as TextView
        mTextViewToAccountName = findViewById<View>(R.id.text_to_account_name) as TextView
        mTextViewToAccountId = findViewById<View>(R.id.text_to_account_id) as TextView
        mTextViewFeeValue = findViewById<View>(R.id.text_fee_value) as TextView
        mEditTextNote = findViewById<View>(R.id.edittext_note) as EditText
        mImageViewCopyFromId = findViewById<View>(R.id.image_copy_from_acount) as ImageView
        mImageViewCopyToId = findViewById<View>(R.id.image_copy_to_account) as ImageView
        mTextViewHgcIcon = findViewById<View>(R.id.hgc_image) as TextView

        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setTitle("TRANSACTION DETAILS")
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener { screen.onCloseClick() }
        }

        reloadData()
    }

    fun reloadData() {
        val color: Int = if (txnRecord.isPositive) R.color.positive else R.color.negative
        mTextViewHgcAmount.setTextColor(resources.getColor(color, null))
        mTextViewDollarAmount.setTextColor(resources.getColor(color, null))
        mTextViewHgcIcon.setTextColor(resources.getColor(color, null))

        val nanoCoins = txnRecord.amount
        mTextViewHgcAmount.text = Singleton.formatHGCShort(nanoCoins)
        mTextViewDollarAmount.text = Singleton.formatUSD(Singleton.hgcToUSD(nanoCoins), true)

        val status = "status: ${when (txnRecord.status) {
            TxnRecord.Status.UNKNOWN -> "unknown"
            TxnRecord.Status.FAILED -> "failed"
            TxnRecord.Status.SUCCESS -> "success"
        }}"

        mTextViewTime.text = "${Singleton.getDateFormat(txnRecord.createdDate!!)}  $status"
        mTextViewFromAccountId.text = txnRecord.fromAccId
        mTextViewToAccountId.text = txnRecord.toAccountId

        val fromAccount = txnRecord.fromAccount
        if (fromAccount != null) {
            mTextViewFromAccountName.text = fromAccount.name
        } else
            mTextViewFromAccountName.text = ""


        val toAccount = txnRecord.toAccount
        if (toAccount != null) {
            mTextViewToAccountName.text = toAccount.name
        } else
            mTextViewToAccountName.text = ""

        mEditTextNote.setText(txnRecord.notes)
        mTextViewFeeValue.text = Singleton.formatHGC(txnRecord.fee, true)


        mImageViewCopyFromId.setOnClickListener { txnRecord.fromAccId?.let { screen?.copyAccount(it) } }

        mImageViewCopyToId.setOnClickListener { txnRecord.toAccountId?.let { screen?.copyAccount(it) } }
    }
}
