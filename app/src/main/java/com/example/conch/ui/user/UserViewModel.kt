package com.example.conch.ui.user

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.conch.data.MyResult
import com.example.conch.data.UserRepository
import com.example.conch.data.model.User
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserViewModel(
    application: Application,
    private val userRepository: UserRepository
) : BaseViewModel(application) {

    val updateResult = MutableLiveData<MyResult<User>>()

    suspend fun exitLogin() = withContext(Dispatchers.Default) {
        userRepository.exitLogin()
    }

    suspend fun updatePassword(oldPassword: String, newUserInfo: User) =
        withContext(Dispatchers.Main) {
            val result = userRepository.updatePassword(oldPassword, newUserInfo)
            updateResult.postValue(result)
        }

    suspend fun updateUser(newUserInfo: User) =
        withContext(Dispatchers.Main) {
            val result = userRepository.updateUser(newUserInfo)
            updateResult.postValue(result)
        }
}