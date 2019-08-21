package hedera.hgc.hgcwallet.hapi

import android.content.Context
import com.google.gson.GsonBuilder
import com.hederahashgraph.api.proto.java.NodeAddressBook
import com.hederahashgraph.api.proto.java.NodeAddressBookOrBuilder
import hedera.hgc.hgcwallet.App

import org.json.JSONArray
import org.json.JSONException

import java.io.IOException
import java.util.concurrent.ThreadLocalRandom

import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.TaskExecutor
import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.node.Node
import hedera.hgc.hgcwallet.hapi.tasks.FileContentTask
import hedera.hgc.hgcwallet.unaryPlus
import io.reactivex.Single
import org.json.JSONObject
import java.lang.Exception
import java.util.*

class AddressBook(private val context: Context) {

    private val updateFreq: Long = 1000 * 60 * 60 * 24 * 2 // every 2 days

    init {
        val list = DBHelper.getAllNodes(false)
        if (list.isNullOrEmpty())
            loadListFromAsset()
    }

    fun updateAddressBookIfNeeded() {
        val lastDate = UserSettings.getLongValue(UserSettings.KEY_LAST_SYNC_NODES_AT)
        if (lastDate >= 0 || (Date().time - lastDate > updateFreq))
            updateAddressBook(null)
    }

    fun getNodes(activeOnly: Boolean): List<Node>? {
        var list = DBHelper.getAllNodes(activeOnly)
        if (activeOnly && list.isNullOrEmpty()) {
            list = DBHelper.getAllNodes(false)
        }
        return list
    }

    private fun loadListFromAsset() {
        try {
            val obj = JSONArray(loadJSONFromAsset())
            for (i in 0 until obj.length()) {
                val nodeObject = obj.getJSONObject(i)

                val node = Node.getNode(
                        nodeObject.optString("host"),
                        nodeObject.optInt("port"),
                        nodeObject.optLong("accountNum"),
                        nodeObject.optLong("shardNum"),
                        nodeObject.optLong("realmNum"))

                DBHelper.createNode(node)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    private fun loadJSONFromAsset(): String? {
        var json: String? = null
        try {
            val `is` = context.assets.open(Config.nodeListFileName)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        return json
    }

    private fun syncDB(newNodes: List<Node>) {
        DBHelper.deleteAllNodes()
        newNodes.forEach { DBHelper.createNode(it) }
    }

    private fun updateAddressBook(onComplete: ((error: String?) -> Unit)?) {
        Singleton.getMasterAccount()?.let { payer ->
            val task = FileContentTask(payer, Config.fileNumAddressBook)
            val taskExecutor = TaskExecutor()
            taskExecutor.setListner {
                val content = task.fileContent
                if (task.error == null && content != null && !content.isEmpty) {
                    try {
                        val newNodes = NodeAddressBook.parseFrom(content).nodeAddressList
                                .mapNotNull {
                                    try {
                                        Node.from(it)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                        syncDB(newNodes)
                        UserSettings.setValue(UserSettings.KEY_LAST_SYNC_NODES_AT, Date().time)
                        onComplete?.invoke(null)

                    } catch (e: Exception) {
                        task.log("Failed to update address book: ${e.message}")
                        onComplete?.invoke("Failed to update address book")
                    }

                } else {
                    onComplete?.invoke("Failed to update address book")
                }
            }
            taskExecutor.execute(task)
        }

    }

    fun getStringRepresentation(): String {
        var addressList = mutableListOf<HashMap<String, Any?>>()
        getNodes(false)?.forEach {
            val map = hashMapOf<String, Any?>().apply {
                put("ip", it.host)
                put("port", it.port)
                put("accountID", it.accountID().stringRepresentation())
            }
            addressList.add(map)
        }
        return try {
            GsonBuilder().create().toJson(addressList)
        } catch (e: Exception) {
            ""
        }
    }
}