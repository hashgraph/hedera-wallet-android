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

package hedera.hgc.hgcwallet.ui.main.account


import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.ui.auth.AuthActivity
import hedera.hgc.hgcwallet.local_auth.AuthListener
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class AccountCreateScreen(account: Account, titleName: String, image: Boolean) : Screen<AccountCreateView>(), AuthListener {

    data class Params(val account: Account, val title: String, val imageValue: Boolean, var shouldShowPrivateKey: Boolean)

    private val params = Params(account, titleName, image, false)
    override fun createView(context: Context): AccountCreateView {
        return AccountCreateView(context, params)
    }

    override fun onAuthSetupSuccess() {
    }

    override fun onAuthSuccess(requestCode: Int) {
        if (requestCode == 100)
            view?.reload()

    }

    override fun onAuthSetupFailed(isCancelled: Boolean) {
    }

    override fun onAuthFailed(requestCode: Int, isCancelled: Boolean) {
    }

    internal fun displayPrivateKey(show: Boolean) {
        params.shouldShowPrivateKey = show
        if (show)
            (activity as? AuthActivity)?.requestAuth(100)
    }

    internal fun onCopyPublicKeyClick() {
        val publickey = Singleton.publicKeyString(params.account)
        val privatekey = Singleton.privateKeyString(params.account)
        Singleton.copyToClipBoard("$publickey || $privatekey", activity)
        Singleton.showToast(activity, activity.getString(R.string.copy_data_clipboard_message))
    }

    internal fun onCopyAccountDataClick() {
        Singleton.accountToJSONString(params.account, params.shouldShowPrivateKey)?.let { accountData ->
            Singleton.copyToClipBoard(accountData, activity)
            Singleton.showToast(activity, activity.getString(R.string.copy_data_clipboard_account_data))
        }
    }

    internal fun updateAccountName(text: String) {
        params.account.name = text
        DBHelper.saveAccount(params.account)
    }

    internal fun updateAccountId(text: String) {
        HGCAccountID.fromString(text)?.let { accountID ->
            params.account.accountID()?.let {
                if (it != accountID)
                    Singleton.clearAccountData(params.account)
            }
            params.account.setAccountID(accountID)
            DBHelper.saveAccount(params.account)
        }
    }
}

class AccountCreateView(context: Context, private val params: AccountCreateScreen.Params) : BaseScreenView<AccountCreateScreen>(context) {
    private val mEditTextNickName: EditText
    private val mEditTextAccountId: EditText
    private val mPublicKey: TextView
    private val mPrivateKey: TextView
    private val mDisplayText: TextView
    private val mHideText: TextView
    private val mCopyImageView: ImageView
    private val mCopyImageViewAccountData: ImageView

    init {
        View.inflate(context, R.layout.view_account_created_layout, this)
        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setTitle(params.title)
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener { screen.navigator.goBack() }
        }

        mEditTextNickName = findViewById<EditText>(R.id.edittext_nick_name)
        mEditTextAccountId = findViewById<EditText>(R.id.edittext_account_id)
        mPublicKey = findViewById<TextView>(R.id.textview_public_address)
        mPrivateKey = findViewById(R.id.textview_private_key)
        mCopyImageView = findViewById<ImageView>(R.id.image_copy)
        mCopyImageViewAccountData = findViewById<ImageView>(R.id.image_copy_account_data)
        mDisplayText = findViewById<TextView>(R.id.text_display)
        mHideText = findViewById<TextView>(R.id.text_hide)


        findViewById<Button>(R.id.btn_close).setOnClickListener { screen.navigator.goBack() }


        mEditTextAccountId.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                screen?.updateAccountId(mEditTextAccountId.text.toString())
            }
        })

        mEditTextNickName.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                screen?.updateAccountName(mEditTextNickName.text.toString())
            }
        })

        mCopyImageView.setOnClickListener {
            val publicKey = mPublicKey.text.toString()
            if (publicKey.isNotEmpty())
                screen?.onCopyPublicKeyClick()

        }

        mCopyImageViewAccountData.setOnClickListener {
            screen?.onCopyAccountDataClick()
        }

        mDisplayText.setOnClickListener {
            AlertDialog.Builder(screen.activity)
                    .setTitle("Warning")
                    .setMessage("There is some risk involved in showing the private key.\n\nDo you really want to see it?")
                    .setPositiveButton("No") { dialogInterface, i -> dialogInterface.dismiss() }.setNegativeButton("Yes") { dialogInterface, i ->
                        dialogInterface.dismiss()
                        screen?.displayPrivateKey(true)
                    }.show()
        }

        mHideText.setOnClickListener {
            screen?.displayPrivateKey(false)
            reload()
        }

        reload()
    }

    fun reload() {

        params.account.let {
            mEditTextNickName.setText(it.name)
            val publickey = Singleton.publicKeyString(it)
            if (publickey.isNotEmpty())
                mPublicKey.text = publickey
        }

        params.account.accountID()?.let {
            mEditTextAccountId.setText(it.stringRepresentation())
        }

        if (params.shouldShowPrivateKey) {
            val privateKey = Singleton.privateKeyString(params.account)
            mDisplayText.visibility = View.GONE
            mHideText.visibility = View.VISIBLE
            mPrivateKey.text = privateKey
        } else {
            mDisplayText.visibility = View.VISIBLE
            mHideText.visibility = View.GONE
            mPrivateKey.setText(R.string.default_private_key)
        }
    }
}
