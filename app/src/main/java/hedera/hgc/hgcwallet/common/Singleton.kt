package hedera.hgc.hgcwallet.common

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.net.ConnectivityManager
import android.support.design.widget.Snackbar
import android.text.format.DateUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.common.io.BaseEncoding
import com.hederahashgraph.api.proto.java.ResponseCodeEnum
import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.crypto.*
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.local_auth.AuthManager
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.modals.HGCKeyType
import hedera.hgc.hgcwallet.modals.KeyDerivation
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

object Singleton {
    private val publicKeyMap = HashMap<Long, String>()
    public val apiLogs = StringBuilder()

    fun createSeed(): HGCSeed {
        return HGCSeed(CryptoUtils.getSecureRandomData(32))
    }

    fun hasWalletSetup(): Boolean {
        try {
            return DBHelper.getMasterWallet() != null
        } catch (e: Exception) {
            return false
        }
    }

    fun setupWallet(keyDerivation: KeyDerivation) {
        DBHelper.createMasterWallet(keyDerivation)
    }

    fun getTotalBalance(): Long {
        var total: Long = 0
        DBHelper.getAllAccounts().forEach {
            total = total + it.balance
        }
        return total
    }

    private fun getKeyChain(): KeyChain {
        var keyChain: KeyChain? = null
        val seed = AuthManager.getSeed()
        DBHelper.getMasterWallet()?.let { wallet ->
            when (wallet.getHGCKeyType()) {
                HGCKeyType.ED25519 -> seed?.let {
                    keyChain = when (wallet.getHGCKeyDerivationType()) {
                        KeyDerivation.BIP32 -> EDBip32KeyChain(it)
                        else -> EDKeyChain(it)
                    }
                }
                else -> Unit
            }
        }

        return keyChain!!
    }

    fun keyForAccount(account: Account): KeyPair {
        return getKeyChain().keyAtIndex(account.accountIndex.toInt())
    }

    fun bytesToString(bytes: ByteArray): String {
        return BaseEncoding.base16().lowerCase().encode(bytes)
    }

    fun publicKeyString(account: Account): String {
        var publicKey: String? = publicKeyMap[account.accountIndex]
        if (publicKey == null) {
            keyForAccount(account).let { keyPair ->
                publicKey = bytesToString(keyPair.getPublicKey())
                publicKeyMap[account.accountIndex] = publicKey!!
            }
        }
        return publicKey!!
    }

    fun clearKeyCache() {
        publicKeyMap.clear()
    }

    fun publicKeyStringShort(account: Account): String {
        return publicKeyLastCharacter(publicKeyString(account), 6)
    }

    fun privateKeyString(account: Account): String {
        val keyPair = keyForAccount(account)
        return bytesToString(keyPair.privateKey)
    }

    fun publicKeyLastCharacter(key: String, value: Int): String {
        return if (key.length > value) {
            key.substring(key.length - value)
        } else {
            key
        }
    }

    fun copyToClipBoard(copyText: String, context: Context) {
        val textClipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val textClip = ClipData.newPlainText("publicKey", copyText)
        textClipboard.primaryClip = textClip
    }

    fun formatUSD(nonoCoins: Long, includeSymbol: Boolean): String {
        return formatUSD(hgcToUSD(nonoCoins), includeSymbol)
    }

    fun formatUSD(amount: Double, includeSymbol: Boolean): String {
        val formatter = NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
            roundingMode = RoundingMode.DOWN
            isGroupingUsed = true
        }

        var moneyString = formatter.format(amount)
        if (includeSymbol)
            moneyString = "$$moneyString"
        return moneyString
    }

    fun formatHGC(nanoCoins: Long, includeSymbol: Boolean): String {
        return formatHGC(toCoins(nanoCoins), includeSymbol)
    }

    fun formatHGC(coins: Double, includeSymbol: Boolean): String {
        return formatHGC(coins, 8, includeSymbol)
    }

    fun formatHGCShort(coins: Double): String {
        return formatHGC(coins, 6, false)
    }

    fun formatHGCShort(nanoCoins: Long): String {
        return formatHGC(toCoins(nanoCoins), 6, false)
    }

    fun formatHGCShort(nanoCoins: Long, includeSymbol: Boolean): String {
        return formatHGC(toCoins(nanoCoins), 6, includeSymbol)
    }

    fun formatHGC(coins: Double, maxFractionDigits: Int, includeSymbol: Boolean): String {
        val formatter = NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = if (maxFractionDigits > 8) maxFractionDigits else 2
            roundingMode = RoundingMode.HALF_UP
            isGroupingUsed = true
        }

        var moneyString = formatter.format(coins)
        if (includeSymbol)
            moneyString = "$moneyString \u210f"
        return moneyString
    }


    fun toCoins(nanoCoins: Long): Double {
        return nanoCoins / 100000000.0
    }

    fun toNanoCoins(coins: Double): Long {
        return (coins * 100000000).toLong()
    }

    fun toString(value: Double): String {
        return String.format("%f", value)
    }

    var conversionRate = 0.0000000012

    fun hgcToUSD(nonoCoins: Long): Double {
        return nonoCoins * conversionRate
    }

    fun USDtoHGC(usd: Double): Long {
        return (usd / conversionRate).toLong()
    }

    fun getDateFormat(value: Date): String {
        try {
            return DateUtils.getRelativeTimeSpanString(value.time, Date().time, DateUtils.MINUTE_IN_MILLIS).toString()
        } catch (e: java.lang.Exception) {
            return ""
        }
    }

    fun showToast(activity: Activity, message: String, length: Int) {
        val toast = Toast.makeText(activity, message, length)
        val view = toast.view
        view.setBackgroundResource(R.color.text_primary)
        val text = view.findViewById<View>(android.R.id.message) as TextView
        text.setTextColor(activity.resources.getColor(R.color.white, null))
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.show()
    }

    fun showToast(activity: Activity, message: String) {
        //   showToast(activity, message, Toast.LENGTH_LONG);
        val rootView = (activity
                .findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                .show()
    }

    fun showDefaultAlert(activity: Activity, title: String, message: String) {
        showDefaultAlert(activity, title, message, null)
    }

    fun showDefaultAlert(activity: Activity, title: String, message: String, listener: DialogInterface.OnClickListener?) {
        AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Dismiss", listener).show()
    }

    fun accountToJSONString(account: Account, includePrivateKey: Boolean): String? {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("accountLabel", account.name)
            jsonObject.put("accountIndex", account.accountIndex)
            jsonObject.put("publicKey", publicKeyString(account))
            if (account.accountID() != null) {
                jsonObject.put("accountID", account.accountID()!!.stringRepresentation())
            }
            if (includePrivateKey == true) {
                jsonObject.put("privateKey", privateKeyString(account))
            }
            return jsonObject.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null
    }

    fun clearAccountData(account: Account) {
        account.balance = 0
        DBHelper.saveAccount(account)
        val isDefaultAcc = account.accountIndex == 0L
        DBHelper.getAllTxnRecord(if (isDefaultAcc) null else account)?.let { records ->
            records.forEach { DBHelper.deleteTxnRecord(it) }
        }
    }

    fun checkInternetConnType(context: Context, networkType: Int): Boolean {
        var hasConnection = false
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.let { cm ->
            cm.activeNetworkInfo?.let { activeNetwork -> hasConnection = activeNetwork.type == networkType }
        }

        return hasConnection
    }

    fun contentFromFile(file: String): String {
        var reader: BufferedReader? = null
        val builder = StringBuilder()
        try {
            reader = BufferedReader(
                    InputStreamReader(App.instance.assets.open(file)))
            var mLine = reader.readLine()
            while (mLine != null) {
                builder.append(mLine + "\n")
                mLine = reader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return builder.toString()
    }

    fun canDoBip32Migration(): Boolean {
        return DBHelper.getMasterWallet()?.run {
            getHGCKeyDerivationType() == KeyDerivation.HGC && getMasterAccountID() != null
        } ?: false
    }

    fun getMasterAccountID(): HGCAccountID? {
        return getMasterAccount()?.accountID()
    }

    fun getMasterAccount(): Account? {
        return DBHelper.getAllAccounts().firstOrNull()
    }

    fun canShowBip39MigrationMessage(): Boolean {
        return DBHelper.getMasterWallet()?.run {
            !UserSettings.getBoolValue(UserSettings.KEY_HAS_SHOWN_BIP39_MNEMONIC)
                    && UserSettings.getBoolValue(UserSettings.KEY_NEEDS_TO_SHOW_BIP39_MNEMONIC)
                    && getHGCKeyDerivationType() == KeyDerivation.BIP32
        } ?: false
    }

    fun getDefaultFee(): Long {
        val defaultFee = UserSettings.getLongValue(UserSettings.KEY_DEFAULT_FEE)
        if (defaultFee == -1L)
            return Config.defaultFee
        else
            return defaultFee
    }

    fun getErrorMessage(code: ResponseCodeEnum): String {
        return when (code) {
            ResponseCodeEnum.OK -> ""
            ResponseCodeEnum.INVALID_TRANSACTION -> "Invalid transaction"
            ResponseCodeEnum.PAYER_ACCOUNT_NOT_FOUND -> "Payer account does not exist"
            ResponseCodeEnum.INVALID_NODE_ACCOUNT -> "Node Account provided does not match the node account of the node the transaction was submitted to"
            ResponseCodeEnum.TRANSACTION_EXPIRED -> "Transaction Expired"
            ResponseCodeEnum.INVALID_TRANSACTION_START -> "Invalid Transaction Start"
            ResponseCodeEnum.INVALID_TRANSACTION_DURATION -> "Invalid Transaction Duration"
            ResponseCodeEnum.INVALID_SIGNATURE -> "Invalid Signature"
            ResponseCodeEnum.MEMO_TOO_LONG -> "Memo Too Long"
            ResponseCodeEnum.INSUFFICIENT_TX_FEE -> "Insufficient TxFee"
            ResponseCodeEnum.INSUFFICIENT_PAYER_BALANCE -> "Insufficient Payer Balance"
            ResponseCodeEnum.DUPLICATE_TRANSACTION -> "Duplicate Transaction"
            ResponseCodeEnum.BUSY -> "Node is busy, please try again in a minute"
            ResponseCodeEnum.NOT_SUPPORTED -> "Not Supported"
            ResponseCodeEnum.INVALID_FILE_ID -> "Invalid FileID"
            ResponseCodeEnum.INVALID_ACCOUNT_ID -> "Invalid AccountID"
            ResponseCodeEnum.INVALID_CONTRACT_ID -> "Invalid ContractID"
            ResponseCodeEnum.INVALID_TRANSACTION_ID -> "Invalid TransactionID"
            ResponseCodeEnum.RECEIPT_NOT_FOUND -> "Receipt NotFound"
            ResponseCodeEnum.RECORD_NOT_FOUND -> "Record NotFound"
            ResponseCodeEnum.INVALID_SOLIDITY_ID -> "InvalidSolidityID"
            ResponseCodeEnum.UNKNOWN -> "unknown"
            ResponseCodeEnum.SUCCESS -> ""
            ResponseCodeEnum.FAIL_INVALID -> "failInvalid"
            ResponseCodeEnum.FAIL_FEE -> "failFee"
            ResponseCodeEnum.FAIL_BALANCE -> "failBalance"
            ResponseCodeEnum.KEY_REQUIRED -> "keyRequired"
            ResponseCodeEnum.BAD_ENCODING -> "badEncoding"
            ResponseCodeEnum.INSUFFICIENT_ACCOUNT_BALANCE -> "insufficientAccountBalance"
            ResponseCodeEnum.UNRECOGNIZED -> "UNRECOGNIZED"
            ResponseCodeEnum.INVALID_SOLIDITY_ADDRESS -> "INVALID SOLIDITY ADDRESS"
            ResponseCodeEnum.INSUFFICIENT_GAS -> "INSUFFICIENT GAS"
            ResponseCodeEnum.CONTRACT_SIZE_LIMIT_EXCEEDED -> "CONTRACT_SIZE_LIMIT_EXCEEDED"
            ResponseCodeEnum.LOCAL_CALL_MODIFICATION_EXCEPTION -> "LOCAL_CALL_MODIFICATION_EXCEPTION"
            ResponseCodeEnum.CONTRACT_REVERT_EXECUTED -> "CONTRACT_REVERT_EXECUTED"
            ResponseCodeEnum.INVALID_RECEIVING_NODE_ACCOUNT -> "INVALID RECEIVING NODE ACCOUNT"
            ResponseCodeEnum.MISSING_QUERY_HEADER -> "MISSING QUERY HEADER"
            ResponseCodeEnum.ACCOUNT_UPDATE_FAILED -> "Account Update Failed"
            ResponseCodeEnum.INVALID_KEY_ENCODING -> "Invalid Key Encoding"
            ResponseCodeEnum.NULL_SOLIDITY_ADDRESS -> "Null Solidity Address"
            ResponseCodeEnum.CONTRACT_UPDATE_FAILED -> "Contract Update Failed"
            ResponseCodeEnum.INVALID_QUERY_HEADER -> "Invalid Query Header"
            ResponseCodeEnum.INVALID_FEE_SUBMITTED -> "Invalid Fee Submitted"
            ResponseCodeEnum.INVALID_PAYER_SIGNATURE -> "Invalid Payer Signature"
            ResponseCodeEnum.KEY_NOT_PROVIDED -> "Key Not Provided"
            ResponseCodeEnum.INVALID_EXPIRATION_TIME -> "Invalid Expiration Time"
            ResponseCodeEnum.NO_WACL_KEY -> "No Wacl Key"
            ResponseCodeEnum.FILE_CONTENT_EMPTY -> "File Content Empty"
            ResponseCodeEnum.INVALID_ACCOUNT_AMOUNTS -> "Invalid Account Amounts"
            ResponseCodeEnum.EMPTY_TRANSACTION_BODY -> "Empty Transaction Body"
            ResponseCodeEnum.INVALID_TRANSACTION_BODY -> "Invalid Transaction Body"
            ResponseCodeEnum.ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS -> "Account Repeated in Account Amounts"

            ResponseCodeEnum.CONTRACT_EXECUTION_EXCEPTION -> "CONTRACT_EXECUTION_EXCEPTION"
            ResponseCodeEnum.INVALID_SIGNATURE_TYPE_MISMATCHING_KEY -> "INVALID_SIGNATURE_TYPE_MISMATCHING_KEY"
            ResponseCodeEnum.INVALID_SIGNATURE_COUNT_MISMATCHING_KEY -> "INVALID_SIGNATURE_COUNT_MISMATCHING_KEY"
            ResponseCodeEnum.EMPTY_CLAIM_BODY -> "EMPTY_CLAIM_BODY"
            ResponseCodeEnum.EMPTY_CLAIM_HASH -> "EMPTY_CLAIM_HASH"
            ResponseCodeEnum.EMPTY_CLAIM_KEYS -> "EMPTY_CLAIM_KEYS"
            ResponseCodeEnum.INVALID_CLAIM_HASH_SIZE -> "INVALID_CLAIM_HASH_SIZE"
            ResponseCodeEnum.EMPTY_QUERY_BODY -> "EMPTY_QUERY_BODY"
            ResponseCodeEnum.EMPTY_CLAIM_QUERY -> "EMPTY_CLAIM_QUERY"
            ResponseCodeEnum.CLAIM_NOT_FOUND -> "CLAIM_NOT_FOUND"
            ResponseCodeEnum.ACCOUNT_ID_DOES_NOT_EXIST -> "ACCOUNT_ID_DOES_NOT_EXIST"
            ResponseCodeEnum.CLAIM_ALREADY_EXISTS -> "CLAIM_ALREADY_EXISTS"
            ResponseCodeEnum.INVALID_FILE_WACL -> "INVALID_FILE_WACL"
            ResponseCodeEnum.SERIALIZATION_FAILED -> "SERIALIZATION_FAILED"
            ResponseCodeEnum.TRANSACTION_OVERSIZE -> "TRANSACTION_OVERSIZE"
            ResponseCodeEnum.TRANSACTION_TOO_MANY_LAYERS -> "TRANSACTION_TOO_MANY_LAYERS"
            ResponseCodeEnum.CONTRACT_DELETED -> "CONTRACT_DELETED"
            ResponseCodeEnum.PLATFORM_NOT_ACTIVE -> "PLATFORM_NOT_ACTIVE"
            ResponseCodeEnum.KEY_PREFIX_MISMATCH -> "KEY_PREFIX_MISMATCH"
            ResponseCodeEnum.PLATFORM_TRANSACTION_NOT_CREATED -> "PLATFORM_TRANSACTION_NOT_CREATED"
            ResponseCodeEnum.INVALID_RENEWAL_PERIOD -> "INVALID_RENEWAL_PERIOD"
            ResponseCodeEnum.INVALID_PAYER_ACCOUNT_ID -> "INVALID_PAYER_ACCOUNT_ID"
            ResponseCodeEnum.ACCOUNT_DELETED -> "ACCOUNT_DELETED"
            ResponseCodeEnum.FILE_DELETED -> "FILE_DELETED"
            ResponseCodeEnum.SETTING_NEGATIVE_ACCOUNT_BALANCE -> "SETTING_NEGATIVE_ACCOUNT_BALANCE"
            ResponseCodeEnum.OBTAINER_REQUIRED -> "OBTAINER_REQUIRED"
            ResponseCodeEnum.OBTAINER_SAME_CONTRACT_ID -> "OBTAINER_SAME_CONTRACT_ID"
            ResponseCodeEnum.OBTAINER_DOES_NOT_EXIST -> "OBTAINER_DOES_NOT_EXIST"
            ResponseCodeEnum.MODIFYING_IMMUTABLE_CONTRACT -> "MODIFYING_IMMUTABLE_CONTRACT"
            ResponseCodeEnum.FILE_SYSTEM_EXCEPTION -> "FILE_SYSTEM_EXCEPTION"
            ResponseCodeEnum.AUTORENEW_DURATION_NOT_IN_RANGE -> "AUTORENEW_DURATION_NOT_IN_RANGE"
            ResponseCodeEnum.ERROR_DECODING_BYTESTRING -> "ERROR_DECODING_BYTESTRING"
            ResponseCodeEnum.CONTRACT_FILE_EMPTY -> "CONTRACT_FILE_EMPTY"
            ResponseCodeEnum.CONTRACT_BYTECODE_EMPTY -> "CONTRACT_BYTECODE_EMPTY"
            ResponseCodeEnum.INVALID_INITIAL_BALANCE -> "INVALID_INITIAL_BALANCE"
            ResponseCodeEnum.INVALID_RECEIVE_RECORD_THRESHOLD -> "INVALID_RECEIVE_RECORD_THRESHOLD"
            ResponseCodeEnum.INVALID_SEND_RECORD_THRESHOLD -> "INVALID_SEND_RECORD_THRESHOLD"
            ResponseCodeEnum.ACCOUNT_IS_NOT_GENESIS_ACCOUNT -> "ACCOUNT_IS_NOT_GENESIS_ACCOUNT"
            ResponseCodeEnum.PAYER_ACCOUNT_UNAUTHORIZED -> "PAYER_ACCOUNT_UNAUTHORIZED"
            ResponseCodeEnum.INVALID_FREEZE_TRANSACTION_BODY -> "INVALID_FREEZE_TRANSACTION_BODY"
            ResponseCodeEnum.FREEZE_TRANSACTION_BODY_NOT_FOUND -> "FREEZE_TRANSACTION_BODY_NOT_FOUND"
            ResponseCodeEnum.TRANSFER_LIST_SIZE_LIMIT_EXCEEDED -> "TRANSFER_LIST_SIZE_LIMIT_EXCEEDED"
            ResponseCodeEnum.RESULT_SIZE_LIMIT_EXCEEDED -> "RESULT_SIZE_LIMIT_EXCEEDED"
            ResponseCodeEnum.NOT_SPECIAL_ACCOUNT -> "NOT_SPECIAL_ACCOUNT"
            ResponseCodeEnum.CONTRACT_NEGATIVE_GAS -> "CONTRACT_NEGATIVE_GAS"
            ResponseCodeEnum.CONTRACT_NEGATIVE_VALUE -> "CONTRACT_NEGATIVE_VALUE"
        }

    }

    fun sendLogcatMail(activity: Activity?, emailTo: String = "") {

        //send file using email
        val emailIntent = Intent(Intent.ACTION_SEND)
        // Set type to "email"
        emailIntent.type = "message/rfc822"
        val to = arrayOf(emailTo)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
        // the attachment
        emailIntent.putExtra(Intent.EXTRA_TEXT, Singleton.apiLogs.toString())
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "android wallet logs")
        activity?.startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }
}