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
import android.widget.EditText
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.modals.KeyDerivation
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import java.util.*
import java.util.regex.Pattern

class RestoreWalletScreen() : Screen<RestoreWalletView>() {

    data class Param(val str: String = "")

    private val param: Param

    init {
        param = Param()
    }

    override fun createView(context: Context): RestoreWalletView {
        return RestoreWalletView(context, param)
    }

    internal fun onCloseButtonClick() {
        navigator?.goBack()
    }

    internal fun onRestoreButtonClick(str: String) {
        try {
            val allWords = Collections
                    .synchronizedList(ArrayList<String>())
            val m = Pattern.compile("[a-zA-Z]+")
                    .matcher(str.toLowerCase())
            while (m.find()) {
                allWords.add(m.group())
            }

            val seed = HGCSeed(allWords)
            var keyDerivation: KeyDerivation? = null
            if (allWords.size != HGCSeed.bip39WordListSize)
                keyDerivation = KeyDerivation.HGC

            navigator?.goTo(RestoreAccountIDScreen(seed, keyDerivation))

        } catch (e: Exception) {
            Singleton.showToast(activity, activity.getString(R.string.invalid_phrase))
            e.printStackTrace()
        }
    }
}

class RestoreWalletView(context: Context, param: RestoreWalletScreen.Param) : BaseScreenView<RestoreWalletScreen>(context) {

    private val edittextRestore: EditText?

    init {
        View.inflate(context, R.layout.view_restore_layout, this)
        TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setTitle(R.string.restore_wallet_account)
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener {
                screen?.onCloseButtonClick()
            }
        }

        edittextRestore = findViewById<EditText>(R.id.edittext_restore)
        findViewById<Button>(R.id.btn_restore)?.apply {
            setOnClickListener {
                screen?.onRestoreButtonClick(edittextRestore.getText().toString())

            }
        }
    }
}