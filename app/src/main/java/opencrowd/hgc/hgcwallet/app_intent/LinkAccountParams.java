package opencrowd.hgc.hgcwallet.app_intent;

import android.net.Uri;

import org.json.JSONObject;

import opencrowd.hgc.hgcwallet.modals.HGCAccountID;
import opencrowd.hgc.hgcwallet.modals.PublicKeyAddress;

public class LinkAccountParams {
    public HGCAccountID accountID;
    public PublicKeyAddress address;
    public Uri redirect;

    public LinkAccountParams(HGCAccountID accountID, PublicKeyAddress address, Uri redirect) {
        this.accountID = accountID;
        this.address = address;
        this.redirect = redirect;
    }

    public static LinkAccountParams from(JSONObject jsonObject) {
        if (jsonObject != null) {
            String action = jsonObject.optString("action","");
            if (action.equals("recvAccountId") || action.equals("setAccountId")) {
                PublicKeyAddress pk = PublicKeyAddress.from(jsonObject.optString("publicKey",""));
                long realmNum = jsonObject.optLong("realmNum",0l);
                long shardNum = jsonObject.optLong("shardNum",0l);
                long accountNum = jsonObject.optLong("accountNum",0l);
                String redirect = jsonObject.optString("redirect","");
                Uri redirectUri = Uri.parse(redirect);

                if (pk != null && !(realmNum == 0 && shardNum == 0 && accountNum == 0)) {
                    return new LinkAccountParams(new HGCAccountID(realmNum, shardNum, accountNum),pk, redirectUri);
                }
            }

        }
        return null;
    }
}
