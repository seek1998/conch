package com.example.conch.data.remote.api

import com.example.conch.data.dto.RegisterInfoVO
import com.example.conch.data.model.User
import com.example.conch.data.remote.ApiResponse
import retrofit2.http.*

interface UserService {
    //登录
    @GET("users")
    suspend fun login(
        @Query("email") email: String,
        @Query("password") password: String
    ): ApiResponse<User>

    //发送注册信息，生成用户
    @POST("users")
    suspend fun register(@Body registerInfo: RegisterInfoVO): ApiResponse<Nothing>

    //向指定邮箱发送验证码
    @GET("users/captcha")
    suspend fun getCaptcha(
        @Query("email") email: String,
        @Query("usage") usage: Int
    ): ApiResponse<Nothing>

    @PUT("users/password")
    suspend fun updatePassword(
        @Query("old") oldPassword: String,
        @Body newUserInfo: User
    ): ApiResponse<User>

    @PUT("users")
    suspend fun update(@Body newUserInfo: User): ApiResponse<User>
}