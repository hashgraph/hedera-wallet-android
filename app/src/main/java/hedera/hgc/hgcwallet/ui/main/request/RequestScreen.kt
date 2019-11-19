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

package hedera.hgc.hgcwallet.ui.main.request


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.app_intent.TransferRequestParams
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.ui.main.account.AccountListScreen

class RequestScreen(fromAccount: Account) : Screen<RequestScreenView>(), AccountListScreen.AccountPickerListener {

    data class Params(var fromAccount: Account, var toName: String = "", var notes: String = "", var amountStr: String = "", var addNote: Boolean = false)

    private val params = Params(fromAccount)

    override fun createView(context: Context): RequestScreenView {
        return RequestScreenView(context, params)
    }

    override fun onAccountPick(account: Account) {
        params.fromAccount = account
        view?.reloadData()
        navigator?.goBackTo(this)
    }

    internal fun onQRButtonTap(amountStr: String, notes: String) {
        val accountId = params.fromAccount.accountID()
        if (accountId != null) {
            //  toAccIdStr = fromAccount.accountID().stringRepresentation();
            params.apply {
                this.amountStr = amountStr
                this.notes = notes
            }

            val toAccountID = HGCAccountID.fromString(accountId.stringRepresentation())
            val coins: Double = amountStr.toDoubleOrNull() ?: 0.0

            val transferRequestParams = TransferRequestParams(toAccountID!!).apply {
                amount = Singleton.toNanoCoins(coins)
                note = notes
            }

            val qrString = transferRequestParams.asQRCode()
            val multiFormatWriter = MultiFormatWriter()
            try {
                val bitMatrix = multiFormatWriter.encode(qrString, BarcodeFormat.QR_CODE, 700, 700)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                view?.updateQRCodeImage(bitmap)
            } catch (e: WriterException) {
                e.printStackTrace()
            }

        } else
            Singleton.showToast(activity, activity.getString(R.string.invalid_account_id))

    }

    internal fun onSendTap(amountStr: String, notes: String) {
        val accountId = params.fromAccount.accountID()
        if (accountId != null) {
            // toAccIdStr = fromAccount.accountID().stringRepresentation();
            params.apply {
                this.amountStr = amountStr
                this.notes = notes
            }

            val toAccountID = HGCAccountID.fromString(accountId.stringRepresentation())
            val coins: Double = amountStr.toDoubleOrNull() ?: 0.0


            val transferRequestParams = TransferRequestParams(toAccountID!!).apply {
                amount = Singleton.toNanoCoins(coins)
                note = notes
                name = UserSettings.getValue(UserSettings.KEY_USER_NAME)
            }

            val sendUrl = transferRequestParams.asUri()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, sendUrl.toString())
                type = "text/plain"
            }

            activity.startActivity(sendIntent)
        } else
            Singleton.showToast(activity, activity.getString(R.string.invalid_account_id))
    }

    internal fun onCloseClick() {
        navigator?.goBack()
    }

    internal fun onChangeTextClick() {
        navigator?.goTo(AccountListScreen(this))
    }
}

class RequestScreenView(context: Context, val params: RequestScreen.Params) : BaseScreenView<RequestScreen>(context) {

    private val mAccountName: TextView
    private val mPublicKey: TextView
    private val mChangeText: TextView
    private val mNoteText: TextView
    private val mNoteEditText: EditText
    private lateinit var mHgcAmountEditText: EditText
    private lateinit var mEditTextDollorAmount: EditText
    private val mAddNote: CheckBox
    private val mQRCode: Button
    private val mSend: Button
    private val mCancel: Button
    private val mQRCodeImage: ImageView
    private val mAmountRelativeLayout: RelativeLayout
    private val mQRGenerateRelativeLayout: RelativeLayout

    private lateinit var dollerEditTextListener: TextWatcher

    private lateinit var hgcEditTextListener: TextWatcher

    init {
        View.inflate(context, R.layout.view_request_layout, this)
        mAccountName = findViewById<View>(R.id.text_account_name) as TextView
        mPublicKey = findViewById<View>(R.id.text_public_key) as TextView
        mChangeText = findViewById<View>(R.id.text_change) as TextView
        mAddNote = findViewById<View>(R.id.add_note) as CheckBox
        //   mRequestNotification = (CheckBox)findViewById(R.id.request_notification);
        mNoteText = findViewById<View>(R.id.text_note) as TextView
        mNoteEditText = findViewById<View>(R.id.edittext_note) as EditText


        mQRCode = findViewById<View>(R.id.btn_qr) as Button
        mSend = findViewById<View>(R.id.btn_send) as Button
        mCancel = findViewById<View>(R.id.btn_cancel) as Button
        mAmountRelativeLayout = findViewById<View>(R.id.amount_relative_layout) as RelativeLayout
        mQRGenerateRelativeLayout = findViewById<View>(R.id.qr_generate_layout) as RelativeLayout
        mQRCodeImage = findViewById<View>(R.id.imageView_qrcode) as ImageView


        dollerEditTextListener = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                try {
                    editable.toString().toDoubleOrNull()?.let { dollar ->
                        val coins = Singleton.toCoins(Singleton.USDtoHGC(dollar))
                        setText(mHgcAmountEditText, hgcEditTextListener, Singleton.toString(coins))
                    }
                } catch (e: Exception) {
                }

            }
        }

        hgcEditTextListener = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                try {
                    editable.toString().toDoubleOrNull()?.let { coins ->
                        val dollar = Singleton.hgcToUSD(Singleton.toNanoCoins(coins))
                        setText(mEditTextDollorAmount, dollerEditTextListener, Singleton.toString(dollar))
                    }
                } catch (e: Exception) {
                }

            }
        }

        mHgcAmountEditText = findViewById<EditText>(R.id.edittext_hgc_amount).apply {
            addTextChangedListener(hgcEditTextListener)
        }
        mEditTextDollorAmount = findViewById<EditText>(R.id.edittext_dollor_amount).apply {
            addTextChangedListener(dollerEditTextListener)
        }

        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setTitle("REQUEST")
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener { screen?.onCloseClick() }
        }


        mChangeText.setOnClickListener { screen?.onChangeTextClick() }

        mHgcAmountEditText.onFocusChangeListener = OnFocusChangeListener { view, b ->
            if (!b)
                params.amountStr = mHgcAmountEditText.text.toString()

        }

        mAddNote.setOnCheckedChangeListener { buttonView, isChecked ->
            params.addNote = isChecked
            setNoteTextFieldVisible(isChecked)
        }

        mNoteEditText.onFocusChangeListener = OnFocusChangeListener { view, b ->
            if (!b)
                params.notes = mNoteEditText.text.toString()

        }

        mQRCode.setOnClickListener { screen?.onQRButtonTap(mHgcAmountEditText.text.toString(), mNoteEditText.text.toString()) }

        mSend.setOnClickListener { screen?.onSendTap(mHgcAmountEditText.text.toString(), mNoteEditText.text.toString()) }

        mCancel.setOnClickListener { setToAccountViewVisible(false) }



        reloadData()
    }

    fun reloadData() {
        mAccountName.text = params.fromAccount.name

        if (params.fromAccount.accountID() != null) {
            mPublicKey.text = params.fromAccount.accountID()!!.stringRepresentation()
        } else {
            val shortKey = Singleton.publicKeyStringShort(params.fromAccount)
            mPublicKey.text = App.instance.getString(R.string.text_key_short, shortKey)
        }

        setText(mHgcAmountEditText, hgcEditTextListener, params.amountStr)
        mNoteEditText.setText(params.notes)
        mAddNote.isChecked = params.addNote
        setNoteTextFieldVisible(params.addNote)
    }

    fun setToAccountViewVisible(visible: Boolean) {
        if (visible) {
            mAmountRelativeLayout.visibility = View.INVISIBLE
            mQRGenerateRelativeLayout.visibility = View.VISIBLE

        } else {
            mAmountRelativeLayout.visibility = View.VISIBLE
            mQRGenerateRelativeLayout.visibility = View.INVISIBLE
        }
    }

    fun setNoteTextFieldVisible(visible: Boolean) {
        if (visible) {
            mNoteText.visibility = View.VISIBLE
            mNoteEditText.visibility = View.VISIBLE
        } else {
            mNoteText.visibility = View.GONE
            mNoteEditText.visibility = View.GONE
        }
    }

    fun updateQRCodeImage(bmp: Bitmap) {
        mQRCodeImage.setImageBitmap(bmp)
        setToAccountViewVisible(true)
    }

    private fun setText(editText: EditText, textWatcher: TextWatcher, text: String) {
        editText.run {
            removeTextChangedListener(textWatcher)
            setText(text)
            addTextChangedListener(textWatcher)
        }
    }
}
