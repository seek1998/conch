package com.example.conch.ui.login

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.example.conch.data.Result
import com.example.conch.data.UserRepository
import com.example.conch.data.model.User
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(application: Application) : BaseViewModel(application) {

    var loginResult = MutableLiveData<Result<User>>()

    fun login(email: String, password: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                UserRepository.login(email, password)
            }
            loginResult.value = result
        }
    }

    override fun attach(savedInstanceState: Bundle?) {

    }

}