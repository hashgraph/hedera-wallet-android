package hedera.hgc.hgcwallet.ui.main.account


import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.account.Account
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class AddAccountScreen(account: Account?, listener: AddAccountListener) : Screen<AddAccountView>() {

    data class Params(val account: Account? = null, val listener: AddAccountListener)

    val params: Params = Params(account, listener)
    override fun createView(context: Context): AddAccountView {
        return AddAccountView(context, params)
    }

    internal fun onSaveClick(name: String, id: String) {

        HGCAccountID.fromString(id)?.let { accountID ->
            params.account?.let {
                it.name = name
                it.setAccountID(accountID)
                DBHelper.saveAccount(it)
                params.listener.onAccountAdded(it)
            }
        }
    }

    internal fun onAddClick(name: String, id: String) {
        HGCAccountID.fromString(id)?.let {
            DBHelper.createExternalAccount(name, it)?.let {
                params.listener.onAccountAdded(it)
            }
        }
    }

    internal fun onCancelClick() {
        navigator?.goBack()
    }

    internal fun onRemoveClick() {
        params.account?.let {
            DBHelper.deleteAccount(it)
            params.listener.onAccountRemoved()
        }
    }

}

interface AddAccountListener {
    fun onAccountAdded(account: Account)
    fun onAccountRemoved()
}

class AddAccountView(context: Context, val params: AddAccountScreen.Params) : BaseScreenView<AddAccountScreen>(context) {

    private val editTextAccountName: EditText
    private val editTextAccountID: EditText
    private val btnRemove: Button
    private val btnAdd: Button
    private val btnSave: Button
    private val layoutAdd: LinearLayout
    private val layoutEdit: LinearLayout


    init {
        View.inflate(context, R.layout.view_add_account_layout, this)
        editTextAccountName = findViewById<EditText>(R.id.edittext_account_name)
        editTextAccountID = findViewById<EditText>(R.id.edittext_account_id)
        btnRemove = findViewById<Button>(R.id.btn_remove).apply {
            setOnClickListener { screen?.onRemoveClick() }
        }

        btnAdd = findViewById<Button>(R.id.btn_add).apply {
            setOnClickListener {
                screen?.onAddClick(editTextAccountName.text.toString(), editTextAccountID.text.toString())
            }
        }
        btnSave = findViewById<Button>(R.id.btn_save).apply {
            setOnClickListener {
                screen?.onSaveClick(editTextAccountName.text.toString(), editTextAccountID.text.toString())
            }
        }
        layoutAdd = findViewById<LinearLayout>(R.id.layout_add)
        layoutEdit = findViewById<LinearLayout>(R.id.layout_edit)
        val titleBar = TitleBarWrapper(findViewById(R.id.titleBar))

        if (params.account == null) {
            layoutAdd.visibility = View.VISIBLE
            layoutEdit.visibility = View.GONE
            titleBar.setTitle("ADD ACCOUNT")
            editTextAccountName.setText("")
            editTextAccountID.setText("")
        } else {
            layoutAdd.visibility = View.GONE
            layoutEdit.visibility = View.VISIBLE
            titleBar.setTitle("EDIT ACCOUNT")
            editTextAccountName.setText(params.account.name)
            params.account.accountID()?.let { editTextAccountID.setText(it.stringRepresentation()) }
        }


        titleBar.setCloseButtonHidden(false)
        titleBar.setOnCloseButtonClickListener { screen?.onCancelClick() }

    }
}
