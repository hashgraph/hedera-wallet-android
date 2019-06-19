package opencrowd.hgc.hgcwallet.ui.onboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;
import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.common.UserSettings;
import opencrowd.hgc.hgcwallet.crypto.HGCSeed;
import opencrowd.hgc.hgcwallet.local_auth.AuthManager;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

import java.util.List;

public class Bip39MigrationScreen extends Screen<Bip39MigrationView> {

    @NonNull
    HGCSeed mSeed;

    public Bip39MigrationScreen() {
        mSeed = AuthManager.INSTANCE.getSeed();
    }

    @NonNull
    @Override
    protected Bip39MigrationView createView(@NonNull Context context) {
        return new Bip39MigrationView(context,mSeed);
    }
}
class Bip39MigrationView extends BaseScreenView<Bip39MigrationScreen> {

    @NonNull
    List<String> mList;
    Button mCopy,mDone;
    private TextView mTextViewCryptoWords;

    public Bip39MigrationView(final @NonNull Context context, final @NonNull HGCSeed seed) {
        super(context);
        inflate(context, R.layout.view_bip39_migration, this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle(getResources().getString(R.string.backup_your_wallet));
        mList = seed.toWordsList();

        mCopy = findViewById(R.id.btn_copy);
        mDone = findViewById(R.id.btn_done);
        mTextViewCryptoWords = findViewById(R.id.textview_crptowords);
        mTextViewCryptoWords.setText(TextUtils.join("   ",mList));

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
                UserSettings.instance.setValue(UserSettings.KEY_HAS_SHOWN_BIP39_MNEMONIC, true);
                getScreen().getNavigator().goBack();
            }
        });
    }
}
