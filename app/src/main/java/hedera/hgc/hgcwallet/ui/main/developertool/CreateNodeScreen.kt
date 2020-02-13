package hedera.hgc.hgcwallet.ui.main.developertool


import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.node.Node
import hedera.hgc.hgcwallet.modals.HGCAccountID
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class CreateNodeScreen(node: Node?) : Screen<CreateNodeScreenView>() {

    data class Params(val node: Node?)

    private val params = Params(node)
    override fun createView(context: Context): CreateNodeScreenView {
        return CreateNodeScreenView(context, params)
    }

    internal fun onSaveClick(host: String, port: String, accountID: String, isDisabled: Boolean) {
        val node = params.node ?: Node()

        if (host.isNotEmpty())
            node.host = host

        if (port.isNotEmpty())
            node.port = Integer.parseInt(port)

        if (accountID.isNotEmpty()) {
            HGCAccountID.fromString(accountID)?.let {
                node.setAccountID(it)
            }
        }
        node.disabled = isDisabled

        if (params.node == null) DBHelper.createNode(node) else DBHelper.updateNode(node)

        navigator?.goBack()
    }
}

class CreateNodeScreenView(context: Context, val params: CreateNodeScreen.Params) : BaseScreenView<CreateNodeScreen>(context) {

    private val mEditTextHost: EditText
    private val mEditTextPort: EditText
    private val mEditTextAccountId: EditText
    private val mCheckBoxIsActive: CheckBox
    private val mButtonsave: Button
    private val titleBar: TitleBarWrapper

    init {
        View.inflate(context, R.layout.view_node_add_layout, this)
        titleBar = TitleBarWrapper(findViewById(R.id.titleBar))

        mEditTextHost = findViewById<EditText>(R.id.edittext_node_host)
        mEditTextPort = findViewById<EditText>(R.id.edittext_node_port)
        mEditTextAccountId = findViewById<EditText>(R.id.edittext_node_account)
        mCheckBoxIsActive = findViewById<CheckBox>(R.id.checkBox_active)
        mButtonsave = findViewById<Button>(R.id.btn_save)


        mButtonsave.setOnClickListener {
            screen?.onSaveClick(mEditTextHost.text.toString(), mEditTextPort.text.toString(),
                    mEditTextAccountId.text.toString(), !mCheckBoxIsActive.isChecked)
        }

        reloadData()
    }


    fun reloadData() {
        titleBar.setTitle(if (params.node == null) "Add Node" else "Edit Node")

        params.node?.let { node ->
            node.host?.let { mEditTextHost.setText(it) }
            mEditTextPort.setText(node.port.toString())
            mEditTextAccountId.setText(node.accountID().stringRepresentation())
            mCheckBoxIsActive.isChecked = !node.disabled
        }

    }
}