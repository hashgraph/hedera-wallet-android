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
import android.content.DialogInterface
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.TaskExecutor
import hedera.hgc.hgcwallet.crypto.EDBip32KeyChain
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.hapi.tasks.MigrateToBip32Task

class Bip32MigrationErrorScreen(seed: HGCSeed, handler: IBip32Migration) : Screen<Bip32MigrationErrorView>() {

    data class Param(val seed: HGCSeed, val handler: IBip32Migration, var hasError: Boolean = false)

    private val param: Param

    init {
        param = Param(seed, handler)
    }

    override fun onShow(context: Context?) {
        super.onShow(context)
        startVerifying()
    }

    override fun createView(context: Context): Bip32MigrationErrorView {
        return Bip32MigrationErrorView(context, param)
    }

    fun startVerifying() {
        val task = MigrateToBip32Task(param.handler.getOldKey(), EDBip32KeyChain(param.seed).keyAtIndex(0), param.handler.getAccountID(), true)
        val taskExecutor = TaskExecutor()
        taskExecutor.setListner {
            if (task.migrationStatus == MigrateToBip32Task.MigrateStatus.Success) {
                Singleton.showDefaultAlert(activity, App.instance.getString(R.string.bip32_migration_success_alert_title), App.instance.getString(R.string.bip32_migration_success_alert_body,param.handler.getAccountID().stringRepresentation()), DialogInterface.OnClickListener { dialogInterface, i ->
                    param.handler.bip32MigrationSuccessful(param.seed, param.handler.getAccountID())
                })
            } else {
                param.hasError = true
                view?.onError()
            }
        }
        taskExecutor.execute(task)
    }


    internal fun goToHelp() {
        Singleton.sendLogcatMail(activity)
    }

    internal fun onCancelButtonClick() {
        param.handler.bip32MigrationAborted()
    }

    internal fun onRetryButtonClick() {
        param.handler.bip32MigrationRetry()
    }
}

class Bip32MigrationErrorView(context: Context, val param: Bip32MigrationErrorScreen.Param) : BaseScreenView<Bip32MigrationErrorScreen>(context) {

    private val indicator: ProgressBar?
    private val helpBtn: Button?
    private val retryBtn: Button?
    private val cancelBtn: Button?
    private val errorDescription: TextView?

    init {
        View.inflate(context, R.layout.view_bip32_migration_error, this)
        indicator = findViewById<ProgressBar>(R.id.loading_indicator)

        errorDescription = findViewById<TextView>(R.id.text_error_description)
        retryBtn = findViewById<Button>(R.id.btn_retry)?.apply {
            setOnClickListener {
                screen?.onRetryButtonClick()
            }
        }

        helpBtn = findViewById<Button>(R.id.btn_help)?.apply {
            setOnClickListener {
                screen?.goToHelp()
            }
        }

        cancelBtn = findViewById<Button>(R.id.btn_cancel)?.apply {
            setOnClickListener {
                screen?.onCancelButtonClick()
            }
        }

        if (param.hasError) {
            onError()
        }

    }

    internal fun onError() {
        errorDescription?.text = "Couldn't verify account update"
        helpBtn?.visibility = View.VISIBLE
        retryBtn?.visibility = View.VISIBLE
        cancelBtn?.visibility = View.GONE
        indicator?.visibility = View.GONE
    }
}