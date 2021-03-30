package com.example.conch.data.remote

import android.content.Context
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider

object ConchOss {

    private const val endpoint = "http://oss-cn-hangzhou.aliyuncs.com"

    private const val stsServer = "http://192.168.3.3:8080/oss/sts"

    // 推荐使用OSSAuthCredentialsProvider。token过期可以及时更新。
    private val credentialProvider = OSSAuthCredentialsProvider(stsServer)

    // 配置类如果不设置，会有默认配置。
    private val conf = ClientConfiguration().apply {
        connectionTimeout = 5 * 1000 // 连接超时，默认15秒。
        socketTimeout = 5 * 1000 // socket超时，默认15秒。
        maxConcurrentRequest = 5 // 最大并发请求数，默认5个。
        maxErrorRetry = 2 // 失败后最大重试次数，默认2次。
    }

    @Volatile
    private var instance: OSSClient? = null

    fun init(applicationContext: Context) {
        synchronized(this) {
            instance ?: OSSClient(applicationContext, endpoint, credentialProvider, conf)
                .also { instance = it }
        }
    }

    fun getInstance() = instance!!

}