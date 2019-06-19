package opencrowd.hgc.hgcwallet.ui.onboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.Config;
import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.modals.HGCKeyType;

public class WalletSetOptionScreen extends Screen<WalletSetOptionView> {

    @NonNull
    @Override
    protected WalletSetOptionView createView(@NonNull Context context) {
        return new WalletSetOptionView(context);
    }
}

class WalletSetOptionView extends BaseScreenView<WalletSetOptionScreen> {

    Button mNewWallet,mExistingWallet;
    public WalletSetOptionView(final @NonNull Context context) {
        super(context);
        inflate(context, R.layout.fragment_wallet_selection, this);
        mNewWallet = (Button)findViewById(R.id.new_wallet);
        mExistingWallet = (Button)findViewById(R.id.restore_wallet);
        mNewWallet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(false);
            }
        });

        mExistingWallet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(true);
            }
        });
    }


    private void onSelect(boolean isRestore) {
        getScreen().getNavigator().goTo(isRestore ? new RestoreWalletScreen(HGCKeyType.ED25519) : new TermsScreen(true, Singleton.INSTANCE.contentFromFile(Config.termsFile), "Terms & Conditions"));
    }

}
