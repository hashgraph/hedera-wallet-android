/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package hedera.hgc.hgcwallet.ui.auth.fingerprint;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import hedera.hgc.hgcwallet.R;
import hedera.hgc.hgcwallet.common.BaseTask;
import hedera.hgc.hgcwallet.common.TaskExecutor;
import hedera.hgc.hgcwallet.local_auth.SecureStorage;
import hedera.hgc.hgcwallet.local_auth.AuthManager;


public class FingerprintActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    public static final String TYPE_UNLOCK = "TYPE_UNLOCK";
    public static final String TYPE_ENABLE = "TYPE_ENABLE";

    private static final String TAG = FingerprintActivity.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    public   FingerprintActivityHelper helper = new FingerprintActivityHelper();
    FingerprintAuthenticationDialogFragment fDialog = null;
    private boolean forSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        forSetup = getIntent().getStringExtra(EXTRA_TYPE).equals(TYPE_ENABLE);
        byte[] iv = SecureStorage.INSTANCE.getIV(SecureStorage.INSTANCE.getKEY_DATA_ENCRYPTION_KEY_FINGERPRINT());
        if (iv == null) {
            iv = SecureStorage.INSTANCE.getIV(SecureStorage.INSTANCE.getKEY_HGC_SEED());
            helper.setup(this, forSetup, iv, iv != null);
        } else {
            helper.setup(this, forSetup, iv, false);
        }


        helper.createKey();
        startAuth();
    }

    /**
     * Proceed the purchase operation
     *
     * @param withFingerprint {@code true} if the purchase was made by using a fingerprint
     * @param cryptoObject the Crypto object
     */
    public void onAuthSuccess(boolean withFingerprint,
            @Nullable final FingerprintManager.CryptoObject cryptoObject) {
        if (withFingerprint) {
            // If the user has authenticated with fingerprint, verify that using cryptography and
            // then show the confirmation message.
            assert cryptoObject != null;

            final View hgcLogo = findViewById(R.id.text_hgc_icon);
            hgcLogo.setVisibility(View.INVISIBLE);
            TaskExecutor taskExecutor = new TaskExecutor();
            taskExecutor.setListner(new TaskExecutor.TaskListner() {
                @Override
                public void onResult(BaseTask task1) {
                    if (task1.error == null) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        if (task1.error.equals("migration-needed")) {
                            if (fDialog != null)
                                fDialog.dismiss();
                            helper = new FingerprintActivityHelper();
                            helper.setup(FingerprintActivity.this, true, null, false);
                            helper.createKey();
                            startAuth();
                        } else {
                            finish();
                        }

                    }

                }
            });
            taskExecutor.execute(new BaseTask() {
                @Override
                public void main() {
                    if (helper.isForSetup()) {
                        AuthManager.INSTANCE.setFingerprintAuth(cryptoObject.getCipher());
                    } else {
                        // Try Migration
                        boolean isVerified = AuthManager.INSTANCE.verifyFingerPrintAuth(cryptoObject.getCipher());
                        if (!isVerified)  {
                            if (AuthManager.INSTANCE.verifyFingerPrintAuthOld(cryptoObject.getCipher())) {
                                error = "migration-needed";
                            } else {
                                error = "";
                            }
                        }
                    }
                };
            });

        }
    }


    private void startAuth() {
        // Set up the crypto object for later. The object will be authenticated by use
        // of the fingerprint.
        if (helper.initCipher()) {

            // Show the fingerprint dialog. The user has the option to use the fingerprint with
            // crypto, or you can fall back to using a server-side verified password.
            FingerprintAuthenticationDialogFragment fragment
                    = new FingerprintAuthenticationDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("forSetup", forSetup);
            fragment.setArguments(bundle);
            fragment.setCryptoObject(new FingerprintManager.CryptoObject(helper.mCipher));
            fragment.setStage(
                    FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
            fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            fDialog = fragment;
        } else {
            // This happens if the lock screen has been disabled or or a fingerprint got
            // enrolled. Thus show the dialog to authenticate with their password first
            // and ask the user if they want to authenticate with fingerprints in the
            // future
            FingerprintAuthenticationDialogFragment fragment
                    = new FingerprintAuthenticationDialogFragment();
            fragment.setCryptoObject(new FingerprintManager.CryptoObject(helper.mCipher));
            fragment.setStage(
                    FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED);
            fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            fDialog = fragment;
        }
    }
}
