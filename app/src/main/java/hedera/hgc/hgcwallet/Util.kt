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

import com.squareup.okhttp.*
import hedera.hgc.hgcwallet.common.Singleton
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

operator fun <T> Flowable<T>.unaryPlus() = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())!!
operator fun <T> Single<T>.unaryPlus() = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())!!
operator fun <T> Maybe<T>.unaryPlus() = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())!!
operator fun Completable.unaryPlus() = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())!!


fun postURL(url: String, body: ByteArray): Single<Boolean> {
    return Single.fromCallable {
        val MEDIA_TYPE_MARKDOWN = MediaType.parse("application/octet-stream")

        val requestBody = RequestBody.create(MEDIA_TYPE_MARKDOWN, body)
        val request = Request.Builder()
                .url(url)
                .post(requestBody).addHeader("Content-Type", "application/octet-stream").addHeader("User-Agent", "HGCApp/")
                .build()

        var success = false

        val client = OkHttpClient()
        client.protocols = Arrays.asList(Protocol.HTTP_1_1)
        try {
            var response: Response = client.newCall(request).execute()
            success = response.isSuccessful

        } catch (e: IOException) {
            e.printStackTrace()
        }

        success
    }
}

fun getByteCount(text: String) = text.toByteArray().count()