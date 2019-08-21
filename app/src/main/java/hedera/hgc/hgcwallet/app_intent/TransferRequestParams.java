package hedera.hgc.hgcwallet.app_intent;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;


import hedera.hgc.hgcwallet.modals.HGCAccountID;

public class TransferRequestParams implements IntentParams, UriConvertible, QRConvertible {
    public HGCAccountID account;
    public long amount = 0;
    public String note, name;
    public boolean notify;

    public TransferRequestParams(HGCAccountID accountID) {
        this.account = accountID;
    }

    @Nullable
    public static TransferRequestParams from(@NonNull Uri uri) {
        String action =  uri.getQueryParameter("action");
        if(action != null && action.equals("payRequest")) {
            HGCAccountID account = HGCAccountID.Companion.fromString(uri.getQueryParameter("acc"));
            if (account != null) {
                TransferRequestParams params = new TransferRequestParams(account);
                try {
                    params.amount = Long.parseLong(uri.getQueryParameter("a"));
                } catch (Exception e){
                }
                params.name = uri.getQueryParameter("name");
                params.note = uri.getQueryParameter("n");
                params.notify = uri.getBooleanQueryParameter("nr",false);
                return params;
            }
        }
        return null;
    }

    public static TransferRequestParams from(JSONObject jsonObject) {
        if (jsonObject != null) {
            String action = jsonObject.optString("action","");
            if (action.equals("payRequest")) {
                HGCAccountID accountID = HGCAccountID.Companion.fromString(jsonObject.optString("acc"));
                if (accountID != null) {
                    TransferRequestParams params = new TransferRequestParams(accountID);
                    params.name = jsonObject.optString("name");
                    params.note = jsonObject.optString("n");
                    params.amount = jsonObject.optLong("a");
                    return params;
                }
            }

        }
        return null;
    }

    @Nullable
    public static TransferRequestParams fromBarCode(@NonNull String code) {
        Uri uri = Uri.parse(code);
        if (uri != null) {
            return from(uri);
        }
        return null;
    }


    @NonNull
    @Override
    public String asQRCode() {
        return asUri().toString();
    }

    @NonNull
    @Override
    public Uri asUri() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(IntentParams.APP_URL_SCHEMA)
                .authority(IntentParams.APP_HOST)
                .appendPath(IntentParams.APP_URL_PATH)
                .appendQueryParameter("action","payRequest")
                .appendQueryParameter("acc",account.stringRepresentation());
        if (amount > 0) {
            builder.appendQueryParameter("a","" + amount);
        }
        if (name != null && !name.isEmpty()) {
            builder.appendQueryParameter("name",name);
        }
        if (note != null && !note.isEmpty()) {
            builder.appendQueryParameter("n",note);
        }
        if (notify) {
            builder.appendQueryParameter("nr","1");
        }
        return builder.build();
    }
}
