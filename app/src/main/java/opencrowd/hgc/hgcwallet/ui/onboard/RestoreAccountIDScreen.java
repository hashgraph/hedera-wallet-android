package opencrowd.hgc.hgcwallet.ui.onboard;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import javax.annotation.Nonnull;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.modals.HGCKeyType;
import opencrowd.hgc.hgcwallet.crypto.HGCSeed;
import opencrowd.hgc.hgcwallet.modals.HGCAccountID;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class RestoreAccountIDScreen extends Screen<RestoreAccountIDView> {

    @NonNull
    HGCKeyType keyType;

    @Nonnull
    HGCSeed seed;

    public RestoreAccountIDScreen(@NonNull HGCKeyType keyType, HGCSeed seed) {
        this.keyType = keyType;
        this.seed = seed;
    }

    @NonNull
    @Override
    protected RestoreAccountIDView createView(@NonNull Context context) {
        return new RestoreAccountIDView(context,keyType);
    }
}

class RestoreAccountIDView extends BaseScreenView<RestoreAccountIDScreen> implements View.OnClickListener {

    Button mRestoreButton;
    ImageView mImageView;
    EditText mEdittextRestore;

    public RestoreAccountIDView(@NonNull Context context, final @NonNull HGCKeyType keyType) {
        super(context);
        inflate(context, R.layout.view_restore_accountid_layout, this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle(R.string.restore_accountid_title);
        titleBar.setCloseButtonHidden(false);
        titleBar.setOnCloseButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getScreen().getNavigator().goBack();
            }
        });

        mImageView = (ImageView)findViewById(R.id.image_close);
        mEdittextRestore = (EditText)findViewById(R.id.edittext_restore);
        mRestoreButton = (Button)findViewById(R.id.btn_restore);
        Button skipButton = (Button)findViewById(R.id.btn_skip);
        mRestoreButton.setOnClickListener(this);
        skipButton.setOnClickListener(this);
        TextView descriptionTv = findViewById(R.id.text_description);
        descriptionTv.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public void onClick(View view) {
        try {
            HGCAccountID accountID = HGCAccountID.fromString(mEdittextRestore.getText().toString());
            Activity activity = getScreen().getActivity();
            getScreen().getNavigator().goTo(new PinSetUpOptionScreen(getScreen().seed,getScreen().keyType, accountID));
            Singleton.INSTANCE.showToast(activity,activity.getString(R.string.wallet_restored));

        } catch (Exception e) {
            Singleton.INSTANCE.showToast(getScreen().getActivity(),getScreen().getActivity().getString(R.string.invalid_phrase));
            e.printStackTrace();
        }
    }
}