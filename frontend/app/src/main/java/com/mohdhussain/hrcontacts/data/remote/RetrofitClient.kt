package com.mohdhussain.hrcontacts.data.remote

import android.content.Context
import com.mohdhussain.hrcontacts.BuildConfig
import com.mohdhussain.hrcontacts.data.auth.TokenManager
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private lateinit var appContext: Context

    // Called once from HrContactsApp.onCreate() before anything touches apiService.
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val moshi: Moshi = Moshi.Builder().build()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(TokenManager.getInstance(appContext)))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    })
                }
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
