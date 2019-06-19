package opencrowd.hgc.hgcwallet.ui.onboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import java.util.List;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.common.UserSettings;
import opencrowd.hgc.hgcwallet.modals.HGCKeyType;
import opencrowd.hgc.hgcwallet.crypto.HGCSeed;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;


public class NewWalletCreateScreen extends Screen<NewWalletCreateView> {

    @NonNull
    HGCSeed mSeed;
    @NonNull HGCKeyType keyType;

    public NewWalletCreateScreen(@NonNull HGCKeyType keyType) {
        mSeed = Singleton.INSTANCE.createSeed();
        this.keyType = keyType;
    }

    @NonNull
    @Override
    protected NewWalletCreateView createView(@NonNull Context context) {
        return new NewWalletCreateView(context,mSeed,keyType);
    }
}
    class NewWalletCreateView extends BaseScreenView<NewWalletCreateScreen> {

        @NonNull List<String> mList;
        Button mCopy,mDone;
        private TextView mTextViewCryptoWords;

        public NewWalletCreateView(final @NonNull Context context, final @NonNull HGCSeed seed, final @NonNull HGCKeyType keyType) {
            super(context);
            inflate(context, R.layout.fragment_new_wallet, this);
            TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
            titleBar.setTitle(getResources().getString(R.string.backup_your_wallet));
            mList = seed.toWordsList();

            mCopy = findViewById(R.id.btn_copy);
            mDone = findViewById(R.id.btn_done);
            mTextViewCryptoWords = findViewById(R.id.textview_crptowords);
            mTextViewCryptoWords.setText(TextUtils.join("   ",mList));

            titleBar.setCloseButtonHidden(false);
            titleBar.setOnCloseButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   getScreen().getNavigator().goBack();
                }
            });

            mCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Singleton.INSTANCE.copyToClipBoard(TextUtils.join(" ",mList),getContext());
                    Singleton.INSTANCE.showToast(getScreen().getActivity(), getScreen().getActivity().getString(R.string.copy_data_clipboard_passphrase));
                }
            });

            mDone.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    getScreen().getNavigator().goTo(new PinSetUpOptionScreen(seed,keyType, null));
                }
            });
            UserSettings.instance.setValue(UserSettings.KEY_HAS_SHOWN_BIP39_MNEMONIC, true);
        }
    }
