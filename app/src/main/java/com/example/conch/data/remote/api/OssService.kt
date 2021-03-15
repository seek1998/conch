package com.example.conch.data.remote.api

import com.example.conch.data.dto.STSToken
import com.example.conch.data.remote.ApiResponse
import retrofit2.http.GET

interface OssService {

    @GET("oss/sst")
    fun getSTS(): ApiResponse<STSToken>
}