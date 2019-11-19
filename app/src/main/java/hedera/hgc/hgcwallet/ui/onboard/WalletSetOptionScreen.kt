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
import android.view.View
import android.widget.Button

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton

class WalletSetOptionScreen : Screen<WalletSetOptionView>() {

    override fun createView(context: Context): WalletSetOptionView {
        return WalletSetOptionView(context)
    }

    internal fun onSelect(restore: Boolean) {
        val nextScreen = if (restore) RestoreWalletScreen() else TermsScreen(true, Singleton.contentFromFile(Config.termsFile), "Terms & Conditions")
        navigator?.goTo(nextScreen)
    }
}

class WalletSetOptionView(context: Context) : BaseScreenView<WalletSetOptionScreen>(context) {

    init {
        View.inflate(context, R.layout.fragment_wallet_selection, this)

        findViewById<Button>(R.id.restore_wallet)?.apply {
            setOnClickListener { screen?.onSelect(true) }
        }
        findViewById<Button>(R.id.new_wallet)?.apply {
            setOnClickListener { screen?.onSelect(false) }
        }


    }


}
