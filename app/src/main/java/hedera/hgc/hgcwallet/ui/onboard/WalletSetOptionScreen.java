package hedera.hgc.hgcwallet.ui.onboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import hedera.hgc.hgcwallet.Config;
import hedera.hgc.hgcwallet.R;
import hedera.hgc.hgcwallet.common.Singleton;

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
        getScreen().getNavigator().goTo(isRestore ? new RestoreWalletScreen() : new TermsScreen(true, Singleton.INSTANCE.contentFromFile(Config.INSTANCE.getTermsFile()), "Terms & Conditions"));
    }

}
