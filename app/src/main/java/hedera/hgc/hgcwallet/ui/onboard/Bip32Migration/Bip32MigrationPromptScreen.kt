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

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.local_auth.AuthManager

class Bip32MigrationPromptScreen(handler: IBip32Migration, forKeyUpdate: Boolean = false) : Screen<Bip32MigrationPromptView>() {

    data class Param(val seed: HGCSeed?, val handler: IBip32Migration, val forKeyUpdate: Boolean)

    private val param: Param

    init {
        param = Param(AuthManager.getSeed(), handler, forKeyUpdate)
    }

    override fun createView(context: Context): Bip32MigrationPromptView {
        return Bip32MigrationPromptView(context, param)
    }

    override fun handleBack(): Boolean {
        return true
    }

    internal fun onAcceptButtonClick() {
        navigator?.goTo(Bip32MigrationBackupScreen(param.handler))
    }

    internal fun onSkipButtonClick() {
        param.handler.bip32MigrationAborted()
    }
}

class Bip32MigrationPromptView(context: Context, val param: Bip32MigrationPromptScreen.Param) : BaseScreenView<Bip32MigrationPromptScreen>(context) {

    init {
        View.inflate(context, R.layout.view_bip32_migration_prompt, this)

        findViewById<Button>(R.id.btn_accept)?.apply {
            setOnClickListener {
                screen?.onAcceptButtonClick()
            }
        }

        findViewById<Button>(R.id.btn_skip)?.apply {
            setOnClickListener {
                screen?.onSkipButtonClick()
            }
        }

        findViewById<TextView>(R.id.text_key_generation)?.apply {
            if (param.forKeyUpdate)
                setText(R.string.update_key_prompt)
        }

    }
}