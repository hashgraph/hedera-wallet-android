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
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper
import hedera.hgc.hgcwallet.ui.onboard.TermsScreen

class AboutScreen : Screen<AboutScreenView>() {
    override fun createView(context: Context): AboutScreenView {
        return AboutScreenView(context)
    }

    internal fun onTermsClick() {
        navigator.goTo(TermsScreen(false, Singleton.contentFromFile(Config.termsFile), "Terms & Conditions"))
    }

    internal fun onPolicyClick() {
        //navigator?.goTo(TermsScreen(false, Singleton.contentFromFile(Config.privacyFile), "Privacy Policy"))
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Config.privacyPolicy))
        activity?.startActivity(browserIntent)
    }

    internal fun sendLogcatMail() {
        Singleton.sendLogcatMail(activity, "")
    }

    internal fun showLicenseWebPage() {
        navigator?.goTo(LicenseScreen())
    }
}

class AboutScreenView(context: Context) : BaseScreenView<AboutScreen>(context) {

    init {
        View.inflate(context, R.layout.view_about_layout, this)
        val titleBar = TitleBarWrapper(findViewById(R.id.titleBar))
        titleBar.setTitle("ABOUT")

        val mTextViewVersion = findViewById<TextView>(R.id.text_version)
        val mTextVeiwBuild = findViewById<TextView>(R.id.text_build)

        findViewById<Button>(R.id.btn_term)?.apply {
            setOnClickListener { screen?.onTermsClick() }
        }

        findViewById<Button>(R.id.btn_privacy_policy)?.apply {
            setOnClickListener { screen?.onPolicyClick() }

        }

        findViewById<Button>(R.id.btn_license)?.apply {
            setOnClickListener { screen?.showLicenseWebPage() }
        }

        setOnLongClickListener {
            screen?.sendLogcatMail()
            false
        }

        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = pInfo.versionName
            val versionCode = pInfo.versionCode
            if (versionName != null) {
                mTextViewVersion.text = "Version $versionName"
            }
            mTextVeiwBuild.text = "Build $versionCode"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }


    }


    //    public static StringBuilder readLogs() {
    //        StringBuilder logBuilder = new StringBuilder();
    //        try {
    //            Process process = Runtime.getRuntime().exec("logcat -d -v long");
    //            BufferedReader bufferedReader = new BufferedReader(
    //                    new InputStreamReader(process.getInputStream()));
    //
    //            String line;
    //            while ((line = bufferedReader.readLine()) != null) {
    //                logBuilder.append(line + "\n");
    //            }
    //        } catch (IOException e) {
    //        }
    //        return logBuilder;
    //    }

}
