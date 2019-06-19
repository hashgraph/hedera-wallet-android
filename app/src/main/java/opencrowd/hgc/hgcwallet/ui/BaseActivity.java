package opencrowd.hgc.hgcwallet.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.wealthfront.magellan.Navigator;
import com.wealthfront.magellan.Screen;
import com.wealthfront.magellan.ScreenLifecycleListener;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.ui.auth.AuthActivity;

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
}
