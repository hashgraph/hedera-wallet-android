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

package hedera.hgc.hgcwallet.ui.main.navigation_menu

import android.content.Context
import android.view.View
import android.webkit.WebView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class LicenseScreen : Screen<LicenseView>() {

    override fun createView(context: Context): LicenseView {
        return LicenseView(context)
    }
}

class LicenseView(context: Context) : BaseScreenView<LicenseScreen>(context) {
    init {
        View.inflate(context, R.layout.view_license, this)
        TitleBarWrapper(findViewById(R.id.titleBar)).setTitle("Licenses")
        findViewById<WebView>(R.id.wv_license)?.apply {
            loadUrl("file:///android_asset/open_source_licenses.html")
        }
    }
}