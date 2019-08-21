package hedera.hgc.hgcwallet.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import hedera.hgc.hgcwallet.R;
import hedera.hgc.hgcwallet.common.Singleton;
import hedera.hgc.hgcwallet.common.UserSettings;
import hedera.hgc.hgcwallet.ui.auth.AuthActivity;
import hedera.hgc.hgcwallet.local_auth.AuthManager;
import hedera.hgc.hgcwallet.ui.main.MainActivity;
import hedera.hgc.hgcwallet.ui.onboard.Bip32Migration.Bip32MigrationActivity;
import hedera.hgc.hgcwallet.ui.onboard.OnboardActivity;

public class LauncherActivity extends AuthActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Intent i = getIntent();
        Uri uri = i.getData();
        if (uri != null) {
            UserSettings.INSTANCE.setValue(UserSettings.KEY_INTENT_URL, uri.toString());
        }
        Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(@Nullable JSONObject referringParams, @Nullable BranchError error) {
                if (error == null) {
                    Log.i("BRANCH SDK", referringParams.toString());
                    UserSettings.INSTANCE.setValue(UserSettings.KEY_BRANCH_PARAMS, referringParams);
                } else {
                    Log.i("BRANCH SDK", error.getMessage());
                }
                launchActivity();
            }
        }, uri);

    }

    private void launchActivity() {
        if (Singleton.INSTANCE.hasWalletSetup()) {
            if (!AuthManager.INSTANCE.hasAuth()) {
                requestAuth(0);
            } else proceed();

        } else {
            Intent intent = new Intent(this, OnboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void proceed() {
        if (Singleton.INSTANCE.canDoBip32Migration())
            startBip32MigrationActivity();
        else
            startMainActivity();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startBip32MigrationActivity() {
        Bip32MigrationActivity.Companion.startActivity(this, false);
        finish();
    }

    private void registerBranch() {

    }

    @Override
    public void onAuthSetupSuccess() {

    }

    @Override
    public void onAuthSuccess(int requestCode) {
        proceed();
    }

    @Override
    public void onAuthFailed(int requestCode, boolean isCancelled) {
        finish();
    }

    @Override
    public void onAuthSetupFailed(boolean isCancelled) {

    }
}
