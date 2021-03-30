package com.example.conch.ui.register

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.conch.data.MyResult
import com.example.conch.data.UserRepository
import com.example.conch.data.dto.RegisterInfoVO
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel(application: Application) : BaseViewModel(application) {

    private val userRepository = UserRepository.getInstance()

    var captchaResult = MutableLiveData<MyResult<Nothing>>()

    var registerResult = MutableLiveData<MyResult<Nothing>>()

    fun getCaptcha(email: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                userRepository.getCaptcha(email, UserRepository.USAGE_REGISTER)
            }
            captchaResult.value = result
        }
    }

    fun register(registerInfo: RegisterInfoVO) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                userRepository.register(registerInfo)
            }
            registerResult.value = result
        }
    }

}