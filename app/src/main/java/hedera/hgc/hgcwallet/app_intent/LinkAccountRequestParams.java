package hedera.hgc.hgcwallet.app_intent;

import android.net.Uri;

import org.json.JSONObject;

public class LinkAccountRequestParams {
    public Uri callback, redirect;

    public LinkAccountRequestParams (Uri callback,  Uri redirect) {
        this.callback = callback;
        this.redirect = redirect;
    }
    public static LinkAccountRequestParams from(JSONObject jsonObject) {
        if (jsonObject != null) {
            String action = jsonObject.optString("action","");
            if (action.equals("requestPublicKey")) {
                String callback = jsonObject.optString("callback","");
                String redirect = jsonObject.optString("redirect","");
                Uri callbackUri = Uri.parse(callback);
                Uri redirectUri = Uri.parse(redirect);
                if (callback != null) {
                    return new LinkAccountRequestParams(callbackUri, redirectUri);
                }
            }

        }
        return null;
    }
}










