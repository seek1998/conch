package com.example.conch.data

import com.example.conch.data.db.ConchRoomDatabase
import com.example.conch.data.model.RegisterInfoVO
import com.example.conch.data.model.User
import com.example.conch.data.remote.Network

class UserRepository(database: ConchRoomDatabase) {

    private val network = Network.getInstance()

    var loggedInUser: User = User()
        private set

    suspend fun login(email: String, password: String): Result<User> {
        val result = network.login(email, password)

        if (result is Result.Success) {
            loggedInUser = result.data as User
        }

        return result
    }

    suspend fun getCaptcha(email: String, usage: Int) = network.getCaptcha(email, usage)

    suspend fun register(registerInfo: RegisterInfoVO) = network.register(registerInfo)

    companion object {

        const val USAGE_REGISTER = 0

        const val USAGE_VERIFY = 1

        @Volatile
        private var instance: UserRepository? = null

        fun init(database: ConchRoomDatabase) {
            synchronized(this) {
                instance ?: UserRepository(database)
                    .also { instance = it }
            }
        }

        fun getInstance() = instance!!
    }

}