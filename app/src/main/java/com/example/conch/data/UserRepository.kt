package com.example.conch.data

import com.example.conch.data.db.ConchRoomDatabase
import com.example.conch.data.local.PersistentStorage
import com.example.conch.data.model.RegisterInfoVO
import com.example.conch.data.model.User
import com.example.conch.data.remote.Network

class UserRepository(
    private val database: ConchRoomDatabase,
    private val storage: PersistentStorage,
    private val network: Network
) {

    var loggedInUser: User = storage.loadLoggedInUser()
        private set

    suspend fun login(email: String, password: String): MyResult<User> {
        val result = network.login(email, password)

        if (result is MyResult.Success) {
            this.loggedInUser = result.data as User
            storage.saveLoggedInUser(this.loggedInUser)
        }

        return result
    }

    suspend fun getCaptcha(email: String, usage: Int) = network.getCaptcha(email, usage)

    suspend fun register(registerInfo: RegisterInfoVO) = network.register(registerInfo)

    fun isLoggedIn(): Boolean {
        return loggedInUser.id != User.LOCAL_USER
    }

    companion object {

        const val USAGE_REGISTER = 0

        const val USAGE_VERIFY = 1

        @Volatile
        private var instance: UserRepository? = null

        fun init(database: ConchRoomDatabase, storage: PersistentStorage, network: Network) {

            synchronized(this) {
                instance ?: UserRepository(database, storage, network)
                    .also { instance = it }
            }
        }

        fun getInstance() = instance!!
    }

}