package hedera.hgc.hgcwallet.ui.main.developertool


import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import java.util.ArrayList

import hedera.hgc.hgcwallet.App
import hedera.hgc.hgcwallet.BuildConfig
import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.database.node.Node
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class NodeScreen : Screen<NodeScreenView>() {

    data class Params(val nodeList: List<Node>, val testMode: Boolean)


    private val param: Params

    init {
        val list = App.instance.addressBook?.getNodes(false);
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


    internal class NodeListAdapter(val context: Context, val nodeList: List<Node>, val testMode: Boolean) : RecyclerView.Adapter<NodeListAdapter.MyViewHolder>() {
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

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
            val itemView = LayoutInflater.from(p0.getContext())
                    .inflate(R.layout.developer_tool_list_row, p0, false)

            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
            val node = nodeList[p1]
            p0.setData(node)
            p0.itemView.setOnClickListener {
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
    }
}
