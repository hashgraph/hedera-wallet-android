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

package hedera.hgc.hgcwallet.ui.onboard


import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.NavigationType
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.TaskExecutor
import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.crypto.EDKeyChain
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.crypto.KeyPair
import hedera.hgc.hgcwallet.hapi.tasks.DetectWalletTask
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.KeyDerivation
import hedera.hgc.hgcwallet.ui.BaseActivity
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.ui.onboard.Bip32Migration.Bip32MigrationPromptScreen
import hedera.hgc.hgcwallet.ui.onboard.Bip32Migration.IBip32Migration

class RestoreAccountIDScreen(seed: HGCSeed, keyDerivation: KeyDerivation?) : Screen<RestoreAccountIDView>(), IBip32Migration {

    data class Param(val seed: HGCSeed, val keyDerivation: KeyDerivation?, var accountId: HGCAccountID?)

    private val param: Param

    init {
        param = Param(seed, keyDerivation, null)
    }

    override fun createView(context: Context): RestoreAccountIDView {
        return RestoreAccountIDView(context, param)
    }

    internal fun titleCloseButtonClicked() {
        navigator?.goBack()
    }

    internal fun goToNextScreen(accountID: HGCAccountID) {
        param.accountId = accountID
        if (param.keyDerivation == null) {
            (activity as? BaseActivity)?.showActivityProgress("Please wait")
            val task = DetectWalletTask(param.seed, accountID)
            val taskExecutor = TaskExecutor()
            taskExecutor.setListner {
                (activity as? BaseActivity)?.hideActivityProgress()
                val kd = task.keyDerivation
                if (kd == null)
                    Singleton.showToast(activity, task.error)
                else
                    askForMigration(kd)
            }
            taskExecutor.execute(task)

        } else {
            askForMigration(param.keyDerivation)
        }
    }

    private fun askForMigration(kd: KeyDerivation) {
        when (kd) {
            KeyDerivation.HGC -> navigator.goTo(Bip32MigrationPromptScreen(this))
            else -> onRestoreSuccess(kd, param.seed)
        }
    }

    private fun onRestoreSuccess(kd: KeyDerivation, seed: HGCSeed) {
        activity?.run {
            Singleton.showToast(this, getString(R.string.wallet_restored))
        }
        navigator?.goTo(PinSetUpOptionScreen(seed, kd, param.accountId))
    }

    override fun getOldKey(): KeyPair {
        return EDKeyChain(param.seed).keyAtIndex(0)
    }
    override fun getAccountID(): HGCAccountID {
        return param.accountId!!
    }

    override fun bip32MigrationAborted() {
        onRestoreSuccess(KeyDerivation.HGC, param.seed)
    }

    override fun bip32MigrationRetry() {
        navigator?.goBackToRoot(NavigationType.GO)
    }

    override fun bip32MigrationSuccessful(newSeed: HGCSeed, accountID: HGCAccountID) {
        UserSettings.setValue(UserSettings.KEY_NEEDS_TO_SHOW_BIP39_MNEMONIC, true)
        onRestoreSuccess(KeyDerivation.BIP32, newSeed)
    }


}

class RestoreAccountIDView(context: Context, val param: RestoreAccountIDScreen.Param) : BaseScreenView<RestoreAccountIDScreen>(context), View.OnClickListener {

    private val restoreButton: Button?
    private val imageView: ImageView?
    private val editTextRestore: EditText?

    init {
        View.inflate(context, R.layout.view_restore_accountid_layout, this)
        val titleBar = TitleBarWrapper(findViewById(R.id.titleBar))
        titleBar.setTitle(R.string.restore_accountid_title)
        titleBar.setCloseButtonHidden(false)
        titleBar.setOnCloseButtonClickListener { screen?.titleCloseButtonClicked() }

        imageView = findViewById<View>(R.id.image_close) as ImageView
        editTextRestore = findViewById<View>(R.id.edittext_restore) as EditText
        restoreButton = findViewById<Button>(R.id.btn_restore)?.apply {
            setOnClickListener(this@RestoreAccountIDView)
        }

        findViewById<TextView>(R.id.text_description)?.apply {
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun onClick(view: View) {
        try {
            val accountID = editTextRestore?.let { HGCAccountID.fromString(it.getText().toString()) }
            val activity = screen.activity
            if (accountID == null) {
                Singleton.showToast(screen.activity, screen.activity.getString(R.string.invalid_account_id))
            } else {
                screen?.goToNextScreen(accountID)
            }


        } catch (e: Exception) {
            Singleton.showToast(screen.activity, screen.activity.getString(R.string.invalid_account_id))
            e.printStackTrace()
        }

    }
}