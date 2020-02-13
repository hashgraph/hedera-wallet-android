package hedera.hgc.hgcwallet

import android.app.Activity
import android.app.Application
import android.os.Bundle

import io.branch.referral.Branch
import hedera.hgc.hgcwallet.hapi.AddressBook
import hedera.hgc.hgcwallet.database.AppDatabase

class App : Application() {
    var database: AppDatabase? = null
    var addressBook: AddressBook? = null

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.USE_TEST_BRANCHIO)
            Branch.enableTestMode()
        Branch.enableLogging()
        Branch.getAutoInstance(this)

        database = AppDatabase.createOrGetAppDatabase(this)

        createAddressBook()

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

    fun createAddressBook() {
        addressBook = AddressBook(this)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}