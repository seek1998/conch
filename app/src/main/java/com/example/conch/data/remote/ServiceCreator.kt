package com.example.conch.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.example.conch.MyApplication
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
object ServiceCreator {

    private val applicationContext: Context = MyApplication.getContext()

    private val httpCacheDirectory = File(applicationContext.cacheDir, "okhttp_cache")

    private val cache: Cache = Cache(httpCacheDirectory, CACHE_SIZE)

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

    /**
     * 判断是否有网络连接
     * @return
     */
    private fun isNetworkConnected(): Boolean {
        val manager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.activeNetworkInfo?.let {
            return it.isAvailable
        }

        return false
    }

    private val requestInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            //拦截请求头 判断哪些接口配置上了缓存（该配置是在retrofit上配置）
            val cacheControl = request.cacheControl.toString()
            //如果没有配置过 那么就是走正常的访问，这里直接return回去了，并没有对请求做处理
            if (cacheControl.trim().isEmpty()) {
                return chain.proceed(request)
            }
            //如果没有网络的情况下，并且配置了缓存，那么我们就设置强制读取缓存，没有读到就抛异常，但是并不会去服务器请求数据
            if (!isNetworkConnected()) {
                val offlineCacheTime = 30
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=$offlineCacheTime")
                    .build()
            }
            return chain.proceed(request)
        }
    }

    private val responseInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        var cacheControl = request.cacheControl.toString()
        //同理获取请求头看看有没有配置过缓存，如果没有配置过就设置成无缓存
        if (cacheControl.trim().isEmpty() or "no-store".contains(cacheControl)) {
            //响应头设置成无缓存
            cacheControl = "no-store"
        }
        response.newBuilder()
            //移除掉Pragma 避免服务器默认返回的Pragma对我们自己的缓存配置产生影响
            .removeHeader("Pragma")
            .header("Cache-Control", cacheControl)
            .build()
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
                .addInterceptor(requestInterceptor)
                .addNetworkInterceptor(responseInterceptor)
                .connectTimeout(5 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(5 * 1000, TimeUnit.MILLISECONDS)
                .cache(cache)
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

private const val CACHE_SIZE: Long = 10 * 10 * 1024//10M