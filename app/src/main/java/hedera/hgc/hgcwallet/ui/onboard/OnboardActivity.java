package hedera.hgc.hgcwallet.ui.onboard;

import android.content.Intent;
import android.os.Bundle;

import com.wealthfront.magellan.Navigator;

import hedera.hgc.hgcwallet.R;
import hedera.hgc.hgcwallet.common.Singleton;
import hedera.hgc.hgcwallet.ui.BaseActivity;
import hedera.hgc.hgcwallet.ui.main.MainActivity;

public class OnboardActivity extends BaseActivity {

    @Override
    protected Navigator createNavigator() {
        return Navigator.withRoot(new WalletSetOptionScreen()).build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);
        if (Singleton.INSTANCE.hasWalletSetup()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onAuthSetupSuccess() {
        PinSetUpOptionScreen screen = (PinSetUpOptionScreen) getNavigator().currentScreen();
        screen.setupWallet();
    }

    @Override
    public void onAuthSuccess(int requestCode) {

    }

    @Override
    public void onAuthFailed(int requestCode,boolean isCancelled) {

    }

    @Override
    public void onAuthSetupFailed(boolean isCancelled) {

    }
}
