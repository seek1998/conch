package com.example.conch.data

import com.example.conch.data.model.RegisterInfoVO
import com.example.conch.data.model.User
import com.example.conch.data.net.Network

object UserRepository {
    
    const val USAGE_REGISTER = 0

    const val USAGE_VERIFY = 1

    var loggedInUser: User? = null
        private set

    suspend fun login(email: String, password: String): Result<User> {
        val result = Network.login(email, password)

        if (result is Result.Success) {
            loggedInUser = result.data
        }

        return result
    }

    suspend fun getCaptcha(email: String, usage: Int) = Network.getCaptcha(email, usage)

    /**
     * @user 用户填写的注册信息
     * @captcha 邮箱验证码
     */
    suspend fun register(registerInfo: RegisterInfoVO) = Network.register(registerInfo)

}