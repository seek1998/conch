package com.example.conch.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
object ServiceCreator {

    private const val BASE_URL = "http://192.168.3.3:8080/"

    private var gson: Gson? = null

    private fun getGson(): Gson? {
        if (gson == null) {
            gson = GsonBuilder()
                .serializeNulls()
                .create()
        }

        return gson
    }

    //Retrofit日志
    private fun httpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message -> Log.i("Retrofit", message) }.setLevel(
            HttpLoggingInterceptor.Level.BODY
        )
    }

    private var client: OkHttpClient? = null

    private fun getClient(): OkHttpClient? {

        if (client == null) {
            client = OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor())
                .connectTimeout(5 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(5 * 1000, TimeUnit.MILLISECONDS)
                .build()
        }

        return client
    }


    private val builder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(getClient())
        .addConverterFactory(GsonConverterFactory.create(getGson()))

    private val retrofit: Retrofit = builder.build()

    fun <T> create(service: Class<T>?): T {
        return retrofit.create(service)
    }
}