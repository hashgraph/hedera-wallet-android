/**
*
* This code is an adaptation of Android OpenSource project codebase
* Attributed to Apache 2.0 License
* https://github.com/googlearchive/android-FingerprintDialog
* src/main/java/com/example/android/fingerprintdialog/SettingsActivity.java
*
*/

package hedera.hgc.hgcwallet.ui.auth.fingerprint;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import androidx.appcompat.app.AppCompatActivity;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

    /**
     * Fragment for settings.
     */
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //addPreferencesFromResource(R.xml.preferences);
        }
    }
}
