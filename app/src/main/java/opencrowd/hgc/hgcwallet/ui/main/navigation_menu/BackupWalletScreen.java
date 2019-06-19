package opencrowd.hgc.hgcwallet.ui.main.navigation_menu;


import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import java.util.ArrayList;
import java.util.List;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.local_auth.AuthManager;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class BackupWalletScreen extends Screen<BackupWalletScreenView>{
    @Override
    protected BackupWalletScreenView createView(Context context) {
        return new BackupWalletScreenView(context);
    }
}

class BackupWalletScreenView extends BaseScreenView<BackupWalletScreen> {

    @NonNull
    List<String> mList = new ArrayList<>();
    Button mCopy;
    private TextView mTextViewCryptoWords;

    public BackupWalletScreenView(Context context) {
        super(context);
        inflate(context, R.layout.view_backup_wallet_layout, this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("BACKUP WALLET");
        titleBar.setCloseButtonHidden(false);
        titleBar.setOnCloseButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getScreen().getNavigator().goBack();
            }
        });

        mList = AuthManager.INSTANCE.getSeed().toWordsList();

        mTextViewCryptoWords = findViewById(R.id.textview_crptowords);
        mTextViewCryptoWords.setText(TextUtils.join("   ",mList));
        mCopy = findViewById(R.id.btn_copy);

        mCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Singleton.INSTANCE.copyToClipBoard(TextUtils.join(" ",mList),getContext());
                Singleton.INSTANCE.showToast(getScreen().getActivity(), getScreen().getActivity().getString(R.string.copy_data_clipboard_passphrase));

            }
        });

    }
}


