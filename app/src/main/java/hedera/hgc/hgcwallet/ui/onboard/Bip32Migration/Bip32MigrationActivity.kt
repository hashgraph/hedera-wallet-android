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

package hedera.hgc.hgcwallet.ui.onboard.Bip32Migration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.wealthfront.magellan.NavigationType
import com.wealthfront.magellan.Navigator
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.crypto.KeyPair
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.local_auth.AuthManager
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.KeyDerivation
import hedera.hgc.hgcwallet.ui.BaseActivity
import hedera.hgc.hgcwallet.ui.main.MainActivity

class Bip32MigrationActivity : BaseActivity(), IBip32Migration {
    private var forKeyUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        forKeyUpdate = intent.getBooleanExtra(EXTRA_FOR_KEY_UPDATE, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard)
        findViewById<TextView>(R.id.actionbar_title)?.apply {
            text = "Hedera Wallet"
        }
    }

    override fun getOldKey(): KeyPair {
        return Singleton.keyForAccount(Singleton.getMasterAccount()!!)
    }

    override fun getAccountID(): HGCAccountID {
        return DBHelper.getAllAccounts()[0].accountID()!!
    }

    override fun bip32MigrationAborted() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun bip32MigrationRetry() {
        navigator?.goBackToRoot(NavigationType.GO)
    }

    override fun bip32MigrationSuccessful(newSeed: HGCSeed, accountID: HGCAccountID) {

        DBHelper.getMasterWallet()?.apply {
            setHGCKeyDerivationType(KeyDerivation.BIP32)
            DBHelper.updateWallet(this)
        }

        Singleton.clearKeyCache()
        AuthManager.saveSeed(newSeed.entropy)

        UserSettings.setValue(UserSettings.KEY_NEEDS_TO_SHOW_BIP39_MNEMONIC, true)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onAuthSetupSuccess() {

    }

    override fun onAuthSuccess(requestCode: Int) {

    }

    override fun onAuthSetupFailed(isCancelled: Boolean) {

    }

    override fun onAuthFailed(requestCode: Int, isCancelled: Boolean) {

    }

    override fun createNavigator(): Navigator {
        return Navigator.withRoot(Bip32MigrationPromptScreen(this, forKeyUpdate)).build()
    }

    companion object {
        val EXTRA_FOR_KEY_UPDATE = "EXTRA_FOR_KEY_UPDATE"

        fun startActivity(fromActivity: Activity) {
            val intent = Intent(fromActivity, Bip32MigrationActivity::class.java)
            intent.putExtra(EXTRA_FOR_KEY_UPDATE, !Singleton.canDoBip32Migration())
            fromActivity.startActivity(intent)
        }
    }
}