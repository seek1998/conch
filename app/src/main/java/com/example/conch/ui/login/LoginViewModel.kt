package com.example.conch.ui.login

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.conch.data.MyResult
import com.example.conch.data.UserRepository
import com.example.conch.data.model.User
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(application: Application) : BaseViewModel(application) {

    val userRepository = UserRepository.getInstance()

    var loginResult = MutableLiveData<MyResult<User>>()

    fun login(email: String, password: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                userRepository.login(email, password)
            }
            loginResult.value = result
        }
    }

}