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