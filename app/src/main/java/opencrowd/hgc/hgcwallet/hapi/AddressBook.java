package opencrowd.hgc.hgcwallet.hapi;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import opencrowd.hgc.hgcwallet.Config;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.node.Node;

public class AddressBook {
    private Context context;
    public AddressBook(Context context) {
        this.context = context;
    }

    public List<Node> getList(boolean activeOnly) {
        List<Node> list = DBHelper.getAllNodes(activeOnly);
        if (list == null || list.isEmpty()) {
            loadListFromAsset();
            list = DBHelper.getAllNodes(activeOnly);
        }
        return list;
    }

    private void loadListFromAsset() {
        try {
            JSONArray obj = new JSONArray(loadJSONFromAsset());
            for (int i = 0; i < obj.length(); i++) {
                JSONObject nodeObject = obj.getJSONObject(i);
                Node node = new Node();
                node.setHost(nodeObject.optString("host"));
                node.setPort(nodeObject.optInt("port"));
                node.setAccountNum(nodeObject.optLong("accountNum"));
                node.setShardNum(nodeObject.optLong("shardNum"));
                node.setRealmNum(nodeObject.optLong("realmNum"));
                DBHelper.createNode(node);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = context.getAssets().open(Config.nodeListFileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public Node randomNode() {
        List<Node> nodes = getList(true);
        if (nodes != null && nodes.isEmpty()) {
            nodes = DBHelper.getAllNodes(false);
        }
        int randomNum = ThreadLocalRandom.current().nextInt(0, nodes.size());
        return nodes.get(randomNum);
    }
}
