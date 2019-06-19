package opencrowd.hgc.hgcwallet;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import io.branch.referral.Branch;
import opencrowd.hgc.hgcwallet.hapi.AddressBook;
import opencrowd.hgc.hgcwallet.database.AppDatabase;


public class App extends Application {

    public static App instance;
    public AppDatabase database;
    public AddressBook addressBook;

    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.USE_TEST_BRANCHIO)
            Branch.enableTestMode();
        Branch.enableLogging();
        Branch.getAutoInstance(this);
        requestQueue = Volley.newRequestQueue(this);

        addressBook = new AddressBook(this);
        database = AppDatabase.createOrGetAppDatabase(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public void addNetworkRequest(Request request) {
        requestQueue.add(request);
    }
}
