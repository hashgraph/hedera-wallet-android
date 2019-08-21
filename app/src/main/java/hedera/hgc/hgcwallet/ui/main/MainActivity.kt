package hedera.hgc.hgcwallet.ui.main

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.integration.android.IntentIntegrator
import com.squareup.okhttp.*
import com.wealthfront.magellan.NavigationType
import com.wealthfront.magellan.Navigator
import com.wealthfront.magellan.Screen
import io.grpc.okhttp.internal.Util
import io.reactivex.disposables.CompositeDisposable
import okio.BufferedSink
import okio.Okio
import okio.Source
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.app_intent.LinkAccountParams
import hedera.hgc.hgcwallet.app_intent.LinkAccountRequestParams
import hedera.hgc.hgcwallet.app_intent.TransferRequestParams
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.TaskExecutor
import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.export_key.ExportKeyActivity
import hedera.hgc.hgcwallet.export_key.WebServer
import hedera.hgc.hgcwallet.hapi.tasks.UpdateBalanceTaskAPI
import hedera.hgc.hgcwallet.hapi.tasks.UpdateTransactionsTask
import hedera.hgc.hgcwallet.local_auth.AuthListener
import hedera.hgc.hgcwallet.local_auth.AuthManager
import hedera.hgc.hgcwallet.local_auth.AuthType
import hedera.hgc.hgcwallet.network.API
import hedera.hgc.hgcwallet.postURL
import hedera.hgc.hgcwallet.ui.BaseActivity
import hedera.hgc.hgcwallet.ui.auth.fingerprint.FingerprintActivityHelper
import hedera.hgc.hgcwallet.ui.main.account.AccountCreateScreen
import hedera.hgc.hgcwallet.ui.main.account.AccountListScreen
import hedera.hgc.hgcwallet.ui.main.developertool.NodeScreen
import hedera.hgc.hgcwallet.ui.main.home.AccountBalanceScreen
import hedera.hgc.hgcwallet.ui.main.navigation_menu.AboutScreen
import hedera.hgc.hgcwallet.ui.main.navigation_menu.BackupWalletScreen
import hedera.hgc.hgcwallet.ui.main.pay.PayScreen
import hedera.hgc.hgcwallet.ui.main.pay.PayType
import hedera.hgc.hgcwallet.ui.main.request.RequestListScreen
import hedera.hgc.hgcwallet.ui.main.settings.SettingsScreen
import hedera.hgc.hgcwallet.ui.main.transcation.CreateAccountScreen
import hedera.hgc.hgcwallet.ui.onboard.Bip32Migration.Bip32MigrationActivity
import hedera.hgc.hgcwallet.ui.onboard.Bip32Migration.Bip32MigrationPromptScreen
import hedera.hgc.hgcwallet.ui.scan.QRScanListener
import hedera.hgc.hgcwallet.unaryPlus
import io.reactivex.Single
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : BaseActivity() {

    private var request: TextView? = null
    private var account: TextView? = null
    private var settings: TextView? = null
    private var mHomeImage: ImageView? = null
    private var mSideMenuImage: ImageView? = null
    private var mActionBartitle: TextView? = null
    private val disposable = CompositeDisposable()


    override fun createNavigator(): Navigator {
        return Navigator.withRoot(AccountBalanceScreen()).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//Line Should be reviewed
        setContentView(R.layout.activity_home)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        mHomeImage = findViewById<ImageView>(R.id.image_home)?.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                unCheckAllBottomTab()
                switchToScreen(AccountBalanceScreen())
            }
        }
        mSideMenuImage = findViewById<ImageView>(R.id.image_side_menu)?.apply {
            visibility = View.VISIBLE

            setOnClickListener {
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END)
                } else {
                    drawer.openDrawer(GravityCompat.END)
                }
            }
        }
        request = findViewById<TextView>(R.id.request)?.apply {
            setOnClickListener {
                checkSelectedtab(this, R.drawable.ic_requests_on)
                switchToScreen(RequestListScreen())
            }

        }
        account = findViewById<TextView>(R.id.account)?.apply {
            setOnClickListener {
                checkSelectedtab(this, R.drawable.ic_accounts_on)
                switchToScreen(AccountListScreen(null))
            }
        }
        settings = findViewById<TextView>(R.id.settings)?.apply {
            setOnClickListener {
                checkSelectedtab(this, R.drawable.ic_settings_on)
                switchToScreen(SettingsScreen())
            }
        }
        mActionBartitle = findViewById<TextView>(R.id.actionbar_title)?.apply {
            text = ""
        }

        val syncButton = findViewById<ImageView>(R.id.image_sync)?.apply {
            visibility = View.VISIBLE
            setOnClickListener { synchronizeBalance(true) }
        }

        val navigationView2 = findViewById<NavigationView>(R.id.nav_view2)?.apply {
            menu.findItem(R.id.enable_pin)?.apply {
                if (isPinAuthEnabled())
                    setTitle(R.string.menuItem_change_pin)
                else
                    setTitle(R.string.menuItem_enable_pin)
            }

            menu.findItem(R.id.default_account)?.apply {
                setTitle(DBHelper.getAllAccounts()[0].name)
            }

            setNavigationItemSelectedListener { menuItem ->
                val id = menuItem.itemId
                when (id) {
                    R.id.default_account -> navigator.goTo(AccountCreateScreen(DBHelper.getAllAccounts()[0], "ACCOUNT DETAILS", true))
                    R.id.create_account -> navigator.goTo(CreateAccountScreen(DBHelper.getAllAccounts()[0]))
                    R.id.requests -> navigator.goTo(RequestListScreen())
                    R.id.backup_phrases -> navigator.goTo(BackupWalletScreen())
                    R.id.pay_to_exchange -> onPayToExchangeClick()
                    R.id.export_key -> if (Singleton.checkInternetConnType(this@MainActivity, ConnectivityManager.TYPE_WIFI)) {
                        val ip = WebServer.getWifiIP()
                        if (ip != null) {
                            val intent = Intent(this@MainActivity, ExportKeyActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        } else
                            Singleton.showToast(this@MainActivity, resources.getString(R.string.export_key_not_wifi_msg))
                    } else
                        Singleton.showToast(this@MainActivity, resources.getString(R.string.export_key_not_wifi_msg))
                    R.id.enable_face_fingerprint -> enableFingerprintAuth()
                    R.id.enable_pin -> enablePinAuth()
                    R.id.nodes -> navigator.goTo(NodeScreen())
                    R.id.get_records -> synchronizeData(true, true)
                    R.id.profile -> navigator.goTo(SettingsScreen())
                    R.id.app_info -> navigator.goTo(AboutScreen())
                    R.id.update_key -> Singleton.getMasterAccountID()?.let { Bip32MigrationActivity.startActivity(this@MainActivity, true) }
                            ?: Singleton.showToast(this@MainActivity, resources.getString(R.string.text_account_not_linked))
                }

                drawer.closeDrawer(GravityCompat.END)
                true
            }
        }
    }

    private fun onPayToExchangeClick() {
        AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(R.string.warning_pay_to_exchange)
                .setPositiveButton(R.string.agree) { dialogInterface, i -> navigator.goTo(PayScreen(DBHelper.getAllAccounts()[0], PayType.PayToExchange)) }
                .setNegativeButton(R.string.decline) { dialogInterface, i -> }.create().show()
    }

    fun clearCache() {
        DBHelper.getAllAccounts().forEach {
            it.balance = 0
            DBHelper.saveAccount(it)
        }

        App.instance.database?.txnRecordDao()?.deleteAll()
        unCheckAllBottomTab()
        switchToScreen(AccountBalanceScreen())
        Singleton.showToast(this@MainActivity, "Cache Cleared")
    }

    private fun enableFingerprintAuth() {
        if (AuthManager.authType !== AuthType.FINGER) {
            val helper = FingerprintActivityHelper().apply {
                setup(this@MainActivity, true, null)
            }

            if (helper.errorMsg == null)
                setupAuth(AuthType.FINGER)
            else
                Singleton.showToast(this, helper.errorMsg)
        } else {
            Singleton.showToast(this, resources.getString(R.string.biometric_already_enabled_msg))
        }
    }

    private fun enablePinAuth() {
        //currently on enable pin and change pin we have same UI
        setupAuth(AuthType.PIN)
    }

    private fun isPinAuthEnabled(): Boolean {
        return AuthManager.authType === AuthType.PIN
    }

    fun synchronizeData(showIndicator: Boolean, fetchRecords: Boolean) {
        if (UserSettings.getBoolValue(UserSettings.KEY_ASKED_FOR_QUERY_COST_WARNING)) {
            synchronizeDataPrivate(showIndicator, fetchRecords)

        } else {
            AlertDialog.Builder(this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.warning_query_cost)
                    .setPositiveButton(R.string.yes) { dialogInterface, i ->
                        UserSettings.setValue(UserSettings.KEY_ASKED_FOR_QUERY_COST_WARNING, true)
                        synchronizeDataPrivate(showIndicator, fetchRecords)
                    }
                    .setNegativeButton(R.string.no) { dialogInterface, i -> UserSettings.setValue(UserSettings.KEY_ASKED_FOR_QUERY_COST_WARNING, true) }.create().show()
        }
    }

    private fun synchronizeDataPrivate(showIndicator: Boolean, fetchRecords: Boolean) {

        if (fetchRecords) {
            if (showIndicator)
                showActivityProgress("Fetching records")
            TaskExecutor().apply {
                setListner { task ->
                    hideActivityProgress()
                    if (task.error != null) {
                        Singleton.showToast(this@MainActivity, getString(R.string.failed_to_fetch_records) + "\n" + task.error)
                        reloadHomeTabData()
                    } else {
                        synchronizeBalance(showIndicator)
                    }
                }
            }.execute(UpdateTransactionsTask())
        } else {
            synchronizeBalance(showIndicator)
        }
    }

    fun synchronizeBalance(showIndicator: Boolean) {
        if (showIndicator)
            showActivityProgress("Fetching balances")
        TaskExecutor().apply {
            setListner { task1 ->
                hideActivityProgress()
                task1.error?.let { Singleton.showToast(this@MainActivity, getString(R.string.failed_to_fetch_balances) + "\n" + it) }
                reloadHomeTabData()
            }
        }.execute(UpdateBalanceTaskAPI())
    }

    private fun reloadHomeTabData() {
        switchToScreen(AccountBalanceScreen())
    }

    override fun onResume() {
        super.onResume()
        if (!AuthManager.hasAuth()) {
            requestAuth(0)
        } else {
            checkForPendingURLIntent()
        }
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun checkForPendingURLIntent() {
        val urlString = UserSettings.getValue(UserSettings.KEY_INTENT_URL)
        if (urlString != null) {
            val uri = Uri.parse(urlString)
            if (uri != null) {
                handleURLIntent(uri)
                UserSettings.resetValue(UserSettings.KEY_INTENT_URL)
            }
        }

        val branchParam = UserSettings.getJSONValue(UserSettings.KEY_BRANCH_PARAMS)
        if (branchParam != null) {
            handleBranchParams(branchParam)
            UserSettings.resetValue(UserSettings.KEY_BRANCH_PARAMS)
        }
    }

    private fun handleURLIntent(uri: Uri) {
        /*TransferRequestParams params = TransferRequestParams.from(uri);
        if (params != null) {
            handleTrasferRequestParam(params);
        }*/
    }

    private fun handleBranchParams(params: JSONObject) {
        val linkAccountParams = LinkAccountParams.from(params)
        if (linkAccountParams != null) {
            handleLinkAccount(linkAccountParams)
        } else {
            val linkAccountRequestParams = LinkAccountRequestParams.from(params)
            if (linkAccountRequestParams != null)
                handleLinkAccountRequest(linkAccountRequestParams)
            else {
                val transferRequestParams = TransferRequestParams.from(params)
                if (transferRequestParams != null) {
                    handleTrasferRequestParam(transferRequestParams)
                }
            }
        }
    }

    private fun handleTrasferRequestParam(params: TransferRequestParams) {
        DBHelper.createPayRequest(params.account, params.amount, params.name, params.note)
        DBHelper.createContact(params.account, params.name, "", false)
        request?.let { checkSelectedtab(it, R.drawable.ic_requests_on) }
        switchToScreen(RequestListScreen())
    }

    private fun handleLinkAccount(params: LinkAccountParams) {
        val accounts = DBHelper.getAllAccounts()
        for (account in accounts) {
            val pk = Singleton.publicKeyString(account).trim { it <= ' ' }.toLowerCase()
            if (pk == params.address.stringRepresentation().toLowerCase()) {
                account.setAccountID(params.accountID)
                DBHelper.saveAccount(account)
                Singleton.showDefaultAlert(this@MainActivity, "Account linked", "Your account " + account.name + " is successfully linked with your accountID", DialogInterface.OnClickListener { dialogInterface, i ->
                    if (params.redirect != null) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, params.redirect)
                        startActivity(browserIntent)
                    }
                })

                break
            }
        }
    }

    private fun handleLinkAccountRequest(params: LinkAccountRequestParams) {
        val list = ArrayList<String>()
        val accounts = DBHelper.getAllAccounts()
        val unlinkedAccounts = ArrayList<Account>()
        for (account in accounts) {
            if (account.accountID() == null) {
                unlinkedAccounts.add(account)
                val pk = account.name + " ( ..." + Singleton.publicKeyStringShort(account) + " )"
                list.add(pk)
            }
        }
        if (!list.isEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select account to link")
            val items = list.toTypedArray()

            builder.setItems(items) { dialog, which -> makeLinkAccountRequest(params, unlinkedAccounts[which]) }

            builder.setNegativeButton("Cancel", null)
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun makeLinkAccountRequest(params: LinkAccountRequestParams, account: Account) {
        val keyPair = Singleton.keyForAccount(account)
        disposable.add((+postURL(params.callback.toString(), keyPair.publicKey)
                ).subscribe({
            if (it) {
                Singleton.showDefaultAlert(this@MainActivity, "Sent", "Your request for link account has been sent successfully", DialogInterface.OnClickListener { dialogInterface, i ->
                    if (params.redirect != null) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, params.redirect)
                        startActivity(browserIntent)
                    }
                })
            } else
                Singleton.showDefaultAlert(this@MainActivity, "Error", "Failed to send link account request")

        }, {
            Singleton.showDefaultAlert(this@MainActivity, "Error", "Failed to send link account request")
        }))

    }

    fun switchToScreen(screen: Screen<*>) {
        if (!navigator.atRoot()) {
            navigator.goBackToRoot(NavigationType.NO_ANIM)
        }
        navigator.replaceNow(screen)
    }

    private fun unCheckAllBottomTab() {
        val requestImage = resources.getDrawable(R.drawable.ic_requests, null)
        val accountImage = resources.getDrawable(R.drawable.ic_accounts, null)
        val settingsImage = resources.getDrawable(R.drawable.ic_settings, null)
        request?.run {
            setCompoundDrawablesWithIntrinsicBounds(null, requestImage, null, null)
            setTextColor(resources.getColor(R.color.gray, null))
        }

        account?.run {
            setCompoundDrawablesWithIntrinsicBounds(null, accountImage, null, null)
            setTextColor(resources.getColor(R.color.gray, null))
        }
        settings?.run {
            setCompoundDrawablesWithIntrinsicBounds(null, settingsImage, null, null)
            setTextColor(resources.getColor(R.color.gray, null))
        }
    }

    private fun checkSelectedtab(textView: TextView, drawable: Int) {
        unCheckAllBottomTab()
        val requestImage = resources.getDrawable(drawable, null)
        textView.setCompoundDrawablesWithIntrinsicBounds(null, requestImage, null, null)
        textView.setTextColor(resources.getColor(R.color.tint, null))
    }

    override fun onAuthSetupSuccess() {
    }

    override fun onAuthSuccess(requestCode: Int) {
        checkForPendingURLIntent()
        val currentScreen = navigator.currentScreen()
        if (currentScreen is AuthListener) {
            val listener = currentScreen as AuthListener
            listener.onAuthSuccess(requestCode)
        }
    }

    override fun onAuthSetupFailed(isCancelled: Boolean) {
        val currentScreen = navigator.currentScreen()
        if (currentScreen is AuthListener) {
            val listener = currentScreen as AuthListener
            listener.onAuthSetupFailed(isCancelled)
        }
    }

    override fun onAuthFailed(requestCode: Int, isCancelled: Boolean) {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            val currentScreen = navigator.currentScreen()
            if (currentScreen is QRScanListener) {
                val listener = currentScreen as QRScanListener
                if (result.contents == null) {
                    listener.onQRScanFinished(false, null)
                } else {
                    listener.onQRScanFinished(true, result.contents)
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    inner class RequestBodyUtil {

        fun create(mediaType: MediaType, inputStream: InputStream): RequestBody {
            return object : RequestBody() {
                override fun contentType(): MediaType {
                    return mediaType
                }

                override fun contentLength(): Long {
                    try {
                        return inputStream.available().toLong()
                    } catch (e: IOException) {
                        return 0
                    }

                }

                @Throws(IOException::class)
                override fun writeTo(sink: BufferedSink) {
                    var source: Source? = null
                    try {
                        source = Okio.source(inputStream)
                        sink.writeAll(source!!)
                    } finally {
                        Util.closeQuietly(source)
                    }
                }
            }
        }
    }
}