package hedera.hgc.hgcwallet.ui.auth;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import hedera.hgc.hgcwallet.local_auth.AuthListener;
import hedera.hgc.hgcwallet.local_auth.AuthManager;
import hedera.hgc.hgcwallet.local_auth.AuthType;
import hedera.hgc.hgcwallet.ui.auth.fingerprint.FingerprintActivity;
import hedera.hgc.hgcwallet.ui.auth.pin.PinAuthActivity;

public abstract class AuthActivity extends AppCompatActivity implements AuthListener {
    private static final int REQUEST_CODE_ENABLE = 11;
    private static final int REQUEST_CODE_UNLOCK = 12;
    private static final int REQUEST_CODE_FINGERPRINT = 221;
    private static final int SECURITY_SETTING_REQUEST_CODE = 233;
    private boolean forSetup;
    private int authRequestCode = 0;
    private AuthManager authManager = AuthManager.INSTANCE;

    @Override
    protected void onPause() {
        super.onPause();
        authManager.handleActivityDidPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        authManager.handleActivityDidResume();
    }

    public void requestAuth(int requestCode ) {
        this.authRequestCode = requestCode;
        forSetup = false;
        switch (authManager.getAuthType()) {
            case PIN:
                Intent intent = new Intent(this, PinAuthActivity.class);
                intent.putExtra(PinAuthActivity.EXTRA_TYPE, PinAuthActivity.TYPE_UNLOCK);
                startActivityForResult(intent, REQUEST_CODE_UNLOCK);
                break;

            case FINGER:
                intent = new Intent(this, FingerprintActivity.class);
                intent.putExtra(FingerprintActivity.EXTRA_TYPE, FingerprintActivity.TYPE_UNLOCK);
                startActivityForResult(intent, REQUEST_CODE_FINGERPRINT);
                break;
        }
    }

    public void setupAuth(AuthType authType) {
        forSetup = true;
        switch (authType) {
            case PIN:
                Intent intent = new Intent(this, PinAuthActivity.class);
                intent.putExtra(PinAuthActivity.EXTRA_TYPE, PinAuthActivity.TYPE_ENABLE);
                startActivityForResult(intent, REQUEST_CODE_ENABLE);
                break;
            case FINGER:
                intent = new Intent(this, FingerprintActivity.class);
                intent.putExtra(FingerprintActivity.EXTRA_TYPE, FingerprintActivity.TYPE_ENABLE);
                startActivityForResult(intent, REQUEST_CODE_FINGERPRINT);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            if (requestCode == REQUEST_CODE_ENABLE || (requestCode == REQUEST_CODE_FINGERPRINT && forSetup))
                onAuthSetupFailed(true);
            else
                onAuthFailed(authRequestCode, true);
            return;
        }

        switch (requestCode){
            case REQUEST_CODE_ENABLE:
                onAuthSetupSuccess();
                break;

            case REQUEST_CODE_UNLOCK:
                onAuthSuccess(authRequestCode);
                break;

            case REQUEST_CODE_FINGERPRINT:
                if (resultCode == RESULT_OK) {
                    //If screen lock authentication is success update text
                    if (forSetup) {
                        onAuthSetupSuccess();
                    } else  {
                        onAuthSuccess(authRequestCode);
                    }

                } else {
                    //If screen lock authentication is failed update text
                    //textView.setText(getResources().getString(R.string.unlock_failed));
                }
                break;

            case SECURITY_SETTING_REQUEST_CODE:
                //When user is enabled Security settings then we don't get any kind of RESULT_OK
                //So we need to check whether device has enabled screen lock or not
                if (isDeviceSecure()) {
                    //If screen lock enabled show toast and start intent to authenticate user
                    //Toast.makeText(this, getResources().getString(R.string.device_is_secure), Toast.LENGTH_SHORT).show();
                    authenticateApp();
                } else {
                    //If screen lock is not enabled just update text
                    //textView.setText(getResources().getString(R.string.security_device_cancelled));
                }
                break;
        }
    }


    //method to authenticate app
    private void authenticateApp() {

        //Get the instance of KeyGuardManager
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        //Check if the device version is greater than or equal to Lollipop(21)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Create an intent to open device screen lock screen to authenticate
            //Pass the Screen Lock screen Title and Description
            Intent i = keyguardManager.createConfirmDeviceCredentialIntent("UnlockHGCApp", "UnlockHGCAppDesc");
            try {
                //Start activity for result
                startActivityForResult(i, REQUEST_CODE_FINGERPRINT);
            } catch (Exception e) {

                //If some exception occurs means Screen lock is not set up please set screen lock
                //Open Security screen directly to enable patter lock
                Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                try {

                    //Start activity for result
                    startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE);
                } catch (Exception ex) {

                    //If app is unable to find any Security settings then user has to set screen lock manually
                    //textView.setText(getResources().getString(R.string.setting_label));
                }
            }
        }
    }

    /**
     * method to return whether device has screen lock enabled or not
     **/
    private boolean isDeviceSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        //this method only work whose api level is greater than or equal to Jelly_Bean (16)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure();

        //You can also use keyguardManager.isDeviceSecure(); but it requires API Level 23

    }
}
