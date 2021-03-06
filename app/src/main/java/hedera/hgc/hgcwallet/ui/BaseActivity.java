/*
 *
 *  Copyright 2019 Hedera Hashgraph LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package hedera.hgc.hgcwallet.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.wealthfront.magellan.Navigator;
import com.wealthfront.magellan.Screen;
import com.wealthfront.magellan.ScreenLifecycleListener;

import hedera.hgc.hgcwallet.R;
import hedera.hgc.hgcwallet.ui.auth.AuthActivity;

public abstract class BaseActivity extends AuthActivity {

    private Navigator navigator;

    protected abstract Navigator createNavigator();

    public Navigator getNavigator() {
        return navigator;
    }

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (navigator == null) {
            navigator = createNavigator();
            navigator.addLifecycleListener(new ScreenLifecycleListener() {
                @Override
                public void onShow(Screen screen) {

                }

                @Override
                public void onHide(Screen screen) {
                    hideKeyboard(BaseActivity.this);
                }
            });
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        navigator.onCreate(this, savedInstanceState);
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        navigator.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigator.onResume(this);
    }

    @Override
    protected void onPause() {
        navigator.onPause(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        navigator.onDestroy(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!navigator.handleBack()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        navigator.onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        navigator.onPrepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    public void showActivityProgress(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    public void hideActivityProgress() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        final Configuration override = new Configuration(newBase.getResources().getConfiguration());
        if (override.fontScale > 1.0f)
            override.fontScale = 1.0f;

        //if (override.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_SMALL))
        // override.screenLayout = (override.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK) | Configuration.SCREENLAYOUT_SIZE_SMALL;
        //override.densityDpi = Configuration.DENSITY_DPI_UNDEFINED ;

        applyOverrideConfiguration(override);

    }
}
