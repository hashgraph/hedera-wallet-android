package opencrowd.hgc.hgcwallet.common

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
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
import opencrowd.hgc.hgcwallet.App
import opencrowd.hgc.hgcwallet.R
import opencrowd.hgc.hgcwallet.crypto.*
import opencrowd.hgc.hgcwallet.database.DBHelper
import opencrowd.hgc.hgcwallet.database.account.Account
import opencrowd.hgc.hgcwallet.local_auth.AuthManager
import opencrowd.hgc.hgcwallet.modals.HGCKeyType
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

    fun setupWallet(type: HGCKeyType) {
        DBHelper.createMasterWallet(type)
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
                HGCKeyType.ECDSA384 -> {
                }
                HGCKeyType.ED25519 -> keyChain = EDKeyChain(seed)
                HGCKeyType.RSA3072 -> {
                }
            }
        }

        return keyChain!!
    }

    fun keyForAccount(account: Account): KeyPair {
        return getKeyChain().keyAtIndex(account.accountIndex)
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
        } catch (e:java.lang.Exception) {
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
            else -> "Invalid query"
        }

    }
}
