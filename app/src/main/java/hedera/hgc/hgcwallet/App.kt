package hedera.hgc.hgcwallet

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

import io.branch.referral.Branch
import hedera.hgc.hgcwallet.hapi.AddressBook
import hedera.hgc.hgcwallet.database.AppDatabase

class App : Application() {
    var database: AppDatabase? = null
    var addressBook: AddressBook? = null
    private var requestQueue: RequestQueue? = null

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.USE_TEST_BRANCHIO)
            Branch.enableTestMode()
        Branch.enableLogging()
        Branch.getAutoInstance(this)
        requestQueue = Volley.newRequestQueue(this)

        database = AppDatabase.createOrGetAppDatabase(this)
        addressBook = AddressBook(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(p0: Activity?) {
            }

            override fun onActivityResumed(p0: Activity?) {
            }

            override fun onActivityStarted(p0: Activity?) {
            }

            override fun onActivityDestroyed(p0: Activity?) {
            }

            override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {
            }

            override fun onActivityStopped(p0: Activity?) {
            }

            override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
            }

        })
    }

    fun addNetworkRequest(request: Request<*>) {
        requestQueue?.add(request)
    }


    companion object {
        lateinit var instance: App
            private set
    }
}