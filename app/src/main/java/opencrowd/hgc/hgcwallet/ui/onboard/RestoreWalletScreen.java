package opencrowd.hgc.hgcwallet.ui.onboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.common.UserSettings;
import opencrowd.hgc.hgcwallet.crypto.HGCSeed;
import opencrowd.hgc.hgcwallet.modals.HGCKeyType;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class RestoreWalletScreen extends Screen<RestoreWalletView> {

    @NonNull
    HGCKeyType keyType;

    public RestoreWalletScreen(@NonNull HGCKeyType keyType)
    {
        this.keyType = keyType;
    }

    @NonNull
    @Override
    protected RestoreWalletView createView(@NonNull Context context) {
        return new RestoreWalletView(context,keyType);
    }
}

class RestoreWalletView extends BaseScreenView<RestoreWalletScreen> {

    Button mRestoreButton;
    ImageView mImageView;
    EditText mEdittextRestore;

    public RestoreWalletView(@NonNull Context context, final @NonNull HGCKeyType keyType) {
        super(context);
        inflate(context, R.layout.view_restore_layout, this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle(R.string.restore_wallet_account);

        mImageView = (ImageView)findViewById(R.id.image_close);
        mEdittextRestore = (EditText)findViewById(R.id.edittext_restore);
        mRestoreButton = (Button)findViewById(R.id.btn_restore);
        titleBar.setCloseButtonHidden(false);
        titleBar.setOnCloseButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getScreen().getNavigator().goBack();
            }
        });

        mRestoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    List<String> allWords = Collections
                            .synchronizedList(new ArrayList<String>());
                    Matcher m = Pattern.compile("[a-zA-Z]+")
                            .matcher(mEdittextRestore.getText().toString().toLowerCase());
                    while (m.find()) {
                        allWords.add(m.group());
                    }

                    HGCSeed seed = new HGCSeed(allWords);
                    if (allWords.size() == HGCSeed.bip39WordListSize) {
                        UserSettings.instance.setValue(UserSettings.KEY_HAS_SHOWN_BIP39_MNEMONIC, true);
                    }
                    getScreen().getNavigator().goTo(new RestoreAccountIDScreen(keyType,seed));

                } catch (Exception e) {
                    Singleton.INSTANCE.showToast(getScreen().getActivity(),getScreen().getActivity().getString(R.string.invalid_phrase));
                    e.printStackTrace();
                }
            }
        });

    }
}
