package opencrowd.hgc.hgcwallet.ui.main.developertool;

import android.os.Bundle;

import com.wealthfront.magellan.Navigator;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.ui.BaseActivity;

public class DeveloperToolActivity extends BaseActivity {

    @Override
    protected Navigator createNavigator() {
        return Navigator.withRoot(new DeveloperToolScreen()).build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devloper_tool);
    }

    @Override
    public void onAuthSetupSuccess() {

    }

    @Override
    public void onAuthSuccess(int requestCode) {

    }

    @Override
    public void onAuthSetupFailed(boolean isCancelled) {

    }

    @Override
    public void onAuthFailed(int requestCode,boolean isCancelled) {

    }
}
