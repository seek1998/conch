package com.example.conch.ui.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.conch.data.Result
import com.example.conch.data.UserRepository
import com.example.conch.data.model.RegisterInfoVO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel : ViewModel() {

    var captchaResult = MutableLiveData<Result<Nothing>>()

    var registerResult = MutableLiveData<Result<Nothing>>()

    fun getCaptcha(email: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                UserRepository.getCaptcha(email, UserRepository.USAGE_REGISTER)
            }
            captchaResult.value = result
        }
    }

    fun register(registerInfo: RegisterInfoVO) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                UserRepository.register(registerInfo)
            }
            registerResult.value = result
        }
    }
}