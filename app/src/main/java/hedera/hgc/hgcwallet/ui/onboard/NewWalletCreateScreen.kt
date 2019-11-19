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
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.modals.KeyDerivation
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class NewWalletCreateScreen() : Screen<NewWalletCreateView>() {

    data class Param(val seed: HGCSeed, val list: List<String>)

    private val param: Param

    init {
        val seed = Singleton.createSeed()
        val list = seed.toWordsList()
        param = Param(seed, list)
    }

    override fun createView(context: Context): NewWalletCreateView {
        return NewWalletCreateView(context, param)
    }

    internal fun onCloseButtonClick() {
        navigator?.goBack()
    }

    internal fun onCopyButtonClick() {
        Singleton.copyToClipBoard(TextUtils.join(" ", param.list), activity)
        Singleton.showToast(activity, activity.getString(R.string.copy_data_clipboard_passphrase))
    }

    internal fun onDoneButtonClick() {
        navigator?.goTo(PinSetUpOptionScreen(param.seed, KeyDerivation.BIP32, null));
    }
}

class NewWalletCreateView(context: Context, param: NewWalletCreateScreen.Param) : BaseScreenView<NewWalletCreateScreen>(context) {

    init {
        View.inflate(context, R.layout.fragment_new_wallet, this)
        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setTitle(R.string.backup_your_wallet)
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener {
                screen?.onCloseButtonClick()
            }
        }

        findViewById<TextView>(R.id.textview_crptowords)?.apply {
            text = TextUtils.join("   ", param.list)
        }


        findViewById<Button>(R.id.btn_copy)?.apply {
            setOnClickListener {
                screen?.onCopyButtonClick()
            }
        }

        findViewById<Button>(R.id.btn_done)?.apply {
            setOnClickListener {
                screen?.onDoneButtonClick()
            }
        }

    }
}