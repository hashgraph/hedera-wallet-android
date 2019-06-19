package opencrowd.hgc.hgcwallet.ui.onboard;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import javax.annotation.Nullable;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.modals.HGCKeyType;
import opencrowd.hgc.hgcwallet.crypto.HGCSeed;
import opencrowd.hgc.hgcwallet.modals.HGCAccountID;
import opencrowd.hgc.hgcwallet.ui.auth.AuthActivity;
import opencrowd.hgc.hgcwallet.local_auth.AuthManager;
import opencrowd.hgc.hgcwallet.local_auth.AuthType;
import opencrowd.hgc.hgcwallet.ui.auth.fingerprint.FingerprintActivityHelper;
import opencrowd.hgc.hgcwallet.ui.main.MainActivity;


public class PinSetUpOptionScreen extends Screen<PinSetUpOptionView> {

    @NonNull
    HGCSeed seed;

    @NonNull
    HGCKeyType keyType;

    @Nullable
    HGCAccountID accountID;

    public PinSetUpOptionScreen(@NonNull HGCSeed seed, @NonNull HGCKeyType keyType, @Nullable HGCAccountID accountID) {
        this.seed = seed;
        this.keyType = keyType;
        this.accountID = accountID;
    }

    @NonNull
    @Override
    protected PinSetUpOptionView createView(@NonNull Context context) {
        return new PinSetUpOptionView(context);
    }

    void setupWallet() {
        AuthManager.INSTANCE.saveSeed(seed.getEntropy());
        Singleton.INSTANCE.setupWallet(keyType);
        if (accountID != null) {
            Account defaultAccount = DBHelper.getAllAccounts().get(0);
            defaultAccount.setAccountID(accountID);
            DBHelper.saveAccount(defaultAccount);
        }
        Intent intent = new Intent(getActivity(), MainActivity.class);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

}

class   PinSetUpOptionView extends BaseScreenView<PinSetUpOptionScreen> {

    public PinSetUpOptionView(@NonNull Context context) {
        super(context);
        inflate(context, R.layout.view_pinsetup_layout, this);
        Button pinButton = (Button)findViewById(R.id.btn_setup_pin);
        pinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthActivity authActivity = (AuthActivity) getScreen().getActivity();
                authActivity.setupAuth(AuthType.PIN);
            }
        });

        Button deviceButton = (Button)findViewById(R.id.btn_enable_fingerprint);
        deviceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerprintActivityHelper helper = new FingerprintActivityHelper();
                helper.setup(getContext(), true, null);
                if (helper.errorMsg == null) {
                    AuthActivity authActivity = (AuthActivity) getScreen().getActivity();
                    authActivity.setupAuth(AuthType.FINGER);

                } else {
                    Singleton.INSTANCE.showToast(getScreen().getActivity(),helper.errorMsg);
                }

            }
        });
    }
}
