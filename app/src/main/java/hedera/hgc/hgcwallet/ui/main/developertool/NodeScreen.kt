package hedera.hgc.hgcwallet.ui.main.developertool


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.BuildConfig
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.node.Node
import hedera.hgc.hgcwallet.ui.BaseActivity

class NodeScreen : Screen<NodeScreenView>() {

    data class Params(var nodeList: List<Node>, val testMode: Boolean)


    private val param: Params

    init {
        val list = App.instance.addressBook?.getNodes(false)
        param = Params(list ?: listOf(), BuildConfig.ALLOW_EDITING_NET)

    }

    override fun createView(context: Context): NodeScreenView {
        return NodeScreenView(context, param)
    }

    internal fun onAddNodeClick() {
        onNodeClick(null)
    }

    internal fun onCopyNodesClick() {
        activity?.let {
            App.instance.addressBook?.getStringRepresentation()?.let { str ->
                Singleton.copyToClipBoard(str, it)
                Singleton.showToast(it, it.getString(R.string.copy_data_clipboard_nodes))
            }
        }
    }

    internal fun onNodeClick(node: Node?) {
        navigator?.goTo(CreateNodeScreen(node))
    }

    internal fun onNodeRefresh() {
        (activity as? BaseActivity)?.let { context ->

            AlertDialog.Builder(context).apply {
                setTitle(R.string.warning)
                setMessage(R.string.refresh_nodes_warning)
                setPositiveButton(R.string.cancel, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        p0?.dismiss()
                    }
                })

                setNegativeButton(R.string.proceed, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        p0?.dismiss()
                        refreshNodes(context)
                    }
                })
            }.show()

        }
    }

    private fun refreshNodes(context: BaseActivity) {
        context.showActivityProgress("Please wait")
        App.instance.addressBook?.updateAddressBook { error ->
            context.hideActivityProgress()
            if (error == null) {
                App.instance.addressBook?.getNodes(false)?.let { param.nodeList = it }
                view?.reloadList()
            } else {
                Singleton.showDefaultAlert(context, context.getString(R.string.error), error)
            }
        }
    }
}

class NodeScreenView(context: Context, val params: NodeScreen.Params) : BaseScreenView<NodeScreen>(context) {
    private val nodeListAdapter: NodeListAdapter
    private val addImageView: ImageView?
    private val copyImageView: ImageView?


    init {
        View.inflate(context, R.layout.view_developer_tool, this)
        findViewById<TextView>(R.id.text_Title)?.text = "Nodes"
        copyImageView = findViewById<ImageView>(R.id.image_copy)
        addImageView = findViewById<ImageView>(R.id.image_add)
        findViewById<Button>(R.id.btn_refresh).apply {
            setOnClickListener { screen?.onNodeRefresh() }
        }

        nodeListAdapter = NodeListAdapter(context, params.nodeList, params.testMode).apply {
            setOnNodeClick(object : NodeListAdapter.OnNodeClick {
                override fun onNode(node: Node) {
                    screen?.onNodeClick(node)
                }
            })
        }
        findViewById<RecyclerView>(R.id.tool_list_recyclerview)?.apply {
            setLayoutManager(LinearLayoutManager(context))
            setItemAnimator(DefaultItemAnimator())
            setHasFixedSize(true)
            adapter = nodeListAdapter
        }

        reloadData()
    }

    private fun reloadData() {
        if (params.testMode) {
            copyImageView?.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    screen?.onCopyNodesClick()
                }
            }

            addImageView?.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    screen?.onAddNodeClick()
                }
            }
        }
    }

    fun reloadList() {
        nodeListAdapter.updateList(params.nodeList)
    }

    internal class NodeListAdapter(val context: Context, private var nodeList: List<Node>, val testMode: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        private var nodeClick: OnNodeClick? = null

        inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val mNodeHost: TextView?
            private val mNodeAccountId: TextView?


            init {
                mNodeHost = view.findViewById<TextView>(R.id.text_node_host)
                mNodeAccountId = view.findViewById<TextView>(R.id.text_node_account_id)
            }

            fun setData(node: Node) {
                mNodeHost?.setText("${node.host} : ${node.port}")
                mNodeAccountId?.apply {
                    text = node.accountID().stringRepresentation()
                    visibility = View.VISIBLE
                }

                if (node.disabled) {
                    mNodeHost?.setTextColor(context.resources.getColor(R.color.text_light, null))
                    mNodeAccountId?.setTextColor(context.resources.getColor(R.color.text_light, null))
                } else {
                    mNodeHost?.setTextColor(context.resources.getColor(R.color.text_primary, null))
                    mNodeAccountId?.setTextColor(context.resources.getColor(R.color.text_primary, null))
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.developer_tool_list_row, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val node = nodeList[position]
            (holder as MyViewHolder).setData(node)
            holder.itemView.setOnClickListener {
                if (testMode)
                    nodeClick?.onNode(node)
            }
        }


        override fun getItemCount(): Int {
            return nodeList.size
        }


        internal interface OnNodeClick {
            fun onNode(node: Node)
        }

        fun setOnNodeClick(listener: OnNodeClick?) {
            nodeClick = listener
        }

        fun updateList(list: List<Node>) {
            nodeList = list
            notifyDataSetChanged()
        }
    }
}
