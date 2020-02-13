package hedera.hgc.hgcwallet.ui.onboard.Bip32Migration

import android.content.Context
import android.content.DialogInterface
import android.view.View
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.TaskExecutor
import hedera.hgc.hgcwallet.crypto.EDBip32KeyChain
import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.hapi.tasks.MigrateToBip32Task

class Bip32MigrationConfirmScreen(seed: HGCSeed, handler: IBip32Migration) : Screen<Bip32MigrationConfirmView>() {

    data class Param(val seed: HGCSeed, val handler: IBip32Migration)

    private val param: Param

    init {
        param = Param(seed, handler)
    }

    override fun createView(context: Context): Bip32MigrationConfirmView {
        return Bip32MigrationConfirmView(context, param)
    }

    override fun onShow(context: Context?) {
        super.onShow(context)
        startMigration()
    }

    internal fun startMigration() {

        val task = MigrateToBip32Task(param.handler.getOldKey(), EDBip32KeyChain(param.seed).keyAtIndex(0), param.handler.getAccountID())
        val taskExecutor = TaskExecutor()
        taskExecutor.setListner {
            when (task.migrationStatus) {
                MigrateToBip32Task.MigrateStatus.Success ->
                    Singleton.showDefaultAlert(activity, App.instance.getString(R.string.bip32_migration_success_alert_title), App.instance.getString(R.string.bip32_migration_success_alert_body,param.handler.getAccountID().stringRepresentation()), DialogInterface.OnClickListener { dialogInterface, i ->
                        param.handler.bip32MigrationSuccessful(param.seed, param.handler.getAccountID())
                    })
                MigrateToBip32Task.MigrateStatus.Failed_Network, MigrateToBip32Task.MigrateStatus.Failed_Verfiy_Key_Update, MigrateToBip32Task.MigrateStatus.Failed_Consensus, MigrateToBip32Task.MigrateStatus.Failed_Update -> goToErrorScreen()
                else -> Unit
            }
        }
        taskExecutor.execute(task)

    }

    private fun goToErrorScreen() {
        navigator?.goTo(Bip32MigrationErrorScreen(param.seed, param.handler))
    }
}

class Bip32MigrationConfirmView(context: Context, val param: Bip32MigrationConfirmScreen.Param) : BaseScreenView<Bip32MigrationConfirmScreen>(context) {

    init {
        View.inflate(context, R.layout.view_bip32_migration_confirmation, this)
    }
}