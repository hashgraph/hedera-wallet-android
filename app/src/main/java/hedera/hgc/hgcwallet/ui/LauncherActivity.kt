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

package hedera.hgc.hgcwallet.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log

import org.json.JSONObject

import io.branch.referral.Branch
import io.branch.referral.BranchError
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.ui.auth.AuthActivity
import hedera.hgc.hgcwallet.local_auth.AuthManager
import hedera.hgc.hgcwallet.ui.main.MainActivity
import hedera.hgc.hgcwallet.ui.onboard.Bip32Migration.Bip32MigrationActivity
import hedera.hgc.hgcwallet.ui.onboard.OnboardActivity

class LauncherActivity : AuthActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        val i = intent
        val uri = i.data
        if (uri != null) {
            UserSettings.setValue(UserSettings.KEY_INTENT_URL, uri.toString())
        }
        Branch.getInstance().initSession({ referringParams, error ->
            if (error == null) {
                Log.i("BRANCH SDK", referringParams!!.toString())
                UserSettings.setValue(UserSettings.KEY_BRANCH_PARAMS, referringParams)
            } else {
                Log.i("BRANCH SDK", error.message)
            }
            launchActivity()
        }, uri)

    }

    private fun launchActivity() {
        if (Singleton.hasWalletSetup()) {
            if (!AuthManager.hasAuth()) {
                requestAuth(0)
            } else
                proceed()

        } else {
            val intent = Intent(this, OnboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun proceed() {
        if (Singleton.canDoBip32Migration())
            startBip32MigrationActivity()
        else
            startMainActivity()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startBip32MigrationActivity() {
        Bip32MigrationActivity.startActivity(this)
        finish()
    }


    override fun onAuthSetupSuccess() {

    }

    override fun onAuthSuccess(requestCode: Int) {
        proceed()
    }

    override fun onAuthFailed(requestCode: Int, isCancelled: Boolean) {
        finish()
    }

    override fun onAuthSetupFailed(isCancelled: Boolean) {

    }
}
