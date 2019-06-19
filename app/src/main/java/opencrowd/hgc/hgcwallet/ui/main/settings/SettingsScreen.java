package opencrowd.hgc.hgcwallet.ui.main.settings;


import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.common.UserSettings;
import opencrowd.hgc.hgcwallet.ui.auth.AuthActivity;
import opencrowd.hgc.hgcwallet.local_auth.AuthListener;
import opencrowd.hgc.hgcwallet.local_auth.AuthManager;
import opencrowd.hgc.hgcwallet.local_auth.AuthType;
import opencrowd.hgc.hgcwallet.ui.auth.fingerprint.FingerprintActivityHelper;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class SettingsScreen extends Screen<SettingsScreenView> implements AuthListener {
    @Override
    protected SettingsScreenView createView(@NonNull Context context) {
        return new SettingsScreenView(context);
    }

    @Override
    public void onAuthSetupSuccess() {

    }

    @Override
    public void onAuthSuccess(int requestCode) {

    }

    @Override
    public void onAuthFailed(int requestCode, boolean isCancelled) {

    }

    @Override
    public void onAuthSetupFailed(boolean isCancelled) {
        SettingsScreenView view = getView();
        if (view != null)
            view.reloadUI();
    }
}

class SettingsScreenView extends BaseScreenView<SettingsScreen> {

    private TextView mEdittextName;
    private RadioButton mPinSetup;
    private RadioButton mFingerprintSetup;

    public SettingsScreenView(@NonNull Context context) {
        super(context);
        inflate(context, R.layout.view_settings_layout,this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle(R.string.text_profile);
        mEdittextName = findViewById(R.id.edittext_name);
        mFingerprintSetup = findViewById(R.id.btn_radio_fingerprint);
        mPinSetup = findViewById(R.id.btn_radio_pinsetup);
        reloadUI();

        mEdittextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                UserSettings.instance.setValue(UserSettings.KEY_USER_NAME,mEdittextName.getText().toString());
            }
        });

        mFingerprintSetup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && AuthManager.INSTANCE.getAuthType() != AuthType.FINGER) {
                    FingerprintActivityHelper helper = new FingerprintActivityHelper();
                    helper.setup(getContext(), true, null);
                    if (helper.errorMsg == null) {
                        AuthActivity authActivity = (AuthActivity) getScreen().getActivity();
                        authActivity.setupAuth(AuthType.FINGER);

                    } else {
                        reloadUI();
                        Singleton.INSTANCE.showToast(getScreen().getActivity(),helper.errorMsg);
                    }
                }
            }
        });

        mPinSetup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && AuthManager.INSTANCE.getAuthType() != AuthType.PIN) {
                    AuthActivity authActivity = (AuthActivity) getScreen().getActivity();
                    authActivity.setupAuth(AuthType.PIN);
                }
            }
        });
    }

    public void reloadUI() {
        mFingerprintSetup.setChecked(AuthManager.INSTANCE.getAuthType() == AuthType.FINGER);
        mPinSetup.setChecked(AuthManager.INSTANCE.getAuthType() == AuthType.PIN);
        String name = UserSettings.instance.getValue(UserSettings.KEY_USER_NAME);
        if(name != null && !name.isEmpty()) {
            mEdittextName.setText(name);
        }
    }
}