package opencrowd.hgc.hgcwallet.ui.onboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.modals.HGCKeyType;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class SignatureOptionsScreen extends Screen<SignatureOptionsView> {
    boolean isRestore = false;

//    public  SignatureOptionsScreen(boolean isRestore) {
//        this.isRestore = isRestore;
//    }

    @NonNull
    @Override
    protected SignatureOptionsView createView(@NonNull Context context) {
        return new SignatureOptionsView(context);
    }
}

class SignatureOptionsView extends BaseScreenView<SignatureOptionsScreen> {

    public SignatureOptionsView(final @NonNull Context context) {
        super(context);
        inflate(context, R.layout.view_signature_options, this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("Signature Algorithm");
        titleBar.setCloseButtonHidden(false);
        titleBar.setOnCloseButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goBack();
            }
        });
        Button mEDButton = findViewById(R.id.ED25519);
        Button mECButton = findViewById(R.id.EC384);
        mEDButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(HGCKeyType.ED25519);
            }
        });

        mECButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(HGCKeyType.ECDSA384);
            }
        });
    }


    private void onSelect(HGCKeyType keyType) {
        getScreen().getNavigator().goTo(getScreen().isRestore ? new RestoreWalletScreen(keyType) : new NewWalletCreateScreen(keyType));
    }
}
