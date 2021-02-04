package com.example.conch.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.conch.data.Result
import com.example.conch.data.UserRepository
import com.example.conch.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {

    var loginResult = MutableLiveData<Result<User>>()

    fun login(email: String, password: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                UserRepository.login(email, password)
            }
            loginResult.value = result
        }
    }

}