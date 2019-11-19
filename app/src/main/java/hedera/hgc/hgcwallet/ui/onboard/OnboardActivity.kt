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

import android.content.Intent
import android.os.Bundle

import com.wealthfront.magellan.Navigator

import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.ui.BaseActivity
import hedera.hgc.hgcwallet.ui.main.MainActivity

class OnboardActivity : BaseActivity() {

    override fun createNavigator(): Navigator {
        return Navigator.withRoot(WalletSetOptionScreen()).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard)
        if (Singleton.hasWalletSetup()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onAuthSetupSuccess() {
        (navigator.currentScreen() as? PinSetUpOptionScreen)?.setupWallet()
    }

    override fun onAuthSuccess(requestCode: Int) {

    }

    override fun onAuthFailed(requestCode: Int, isCancelled: Boolean) {

    }

    override fun onAuthSetupFailed(isCancelled: Boolean) {

    }
}
