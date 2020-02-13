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