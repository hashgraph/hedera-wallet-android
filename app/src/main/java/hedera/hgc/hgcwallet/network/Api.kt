package hedera.hgc.hgcwallet.network

import com.google.gson.GsonBuilder
import com.squareup.okhttp.RequestBody
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url


interface API {

    companion object {


        /**
         * Creates the retrofit API instance
         */
        fun create(baseUrl: String): API {
            var url = baseUrl
            if (!url.endsWith("/"))
                url += "/"
            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build()
            val gsonBuilder = GsonBuilder()
            val retrofit =
                    Retrofit.Builder()
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .baseUrl(url)
                            .client(okHttpClient)
                            .build()
            return retrofit.create(API::class.java)
        }
    }
}