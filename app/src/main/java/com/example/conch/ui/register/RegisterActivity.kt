package com.example.conch.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.conch.R
import com.example.conch.data.Result
import com.example.conch.data.model.RegisterInfoVO
import com.example.conch.databinding.ActivityRegisterBinding
import com.example.conch.ui.login.LoginActivity
import com.jaeger.library.StatusBarUtil

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var viewModel: RegisterViewModel

    lateinit var registerInfo: RegisterInfoVO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initDataBinding()
        initToolBar()

        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        binding.btnGetCaptcha.apply {
            setOnClickListener {
                val email = registerInfo.email
                viewModel.getCaptcha(email)
            }
        }

        binding.btnRegister.apply {
            setOnClickListener {
                viewModel.register(registerInfo)
            }
        }

        viewModel.captchaResult.observe(this, {
            val result = it

            if (result is Result.Success) {
                sendCaptchaSucceeded()
            }

            if (result is Result.Error) {
                showErrorMessage(result.exception.message)
            }
        })

        viewModel.registerResult.observe(this, {
            val result = it

            if (result is Result.Success) {
                registerSucceeded()
            }

            if (result is Result.Error) {
                showErrorMessage(result.exception.message)
            }

        })
    }

    private fun registerSucceeded() {
        Toast.makeText(applicationContext, "注册成功", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showErrorMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //注册完成，退出至登录界面
    private fun sendCaptchaSucceeded() {
        Toast.makeText(applicationContext, "验证码已发送", Toast.LENGTH_SHORT).show()
    }

    private fun initToolBar() {
        StatusBarUtil.setDarkMode(this)
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.purple500), 0)
    }

    private fun initDataBinding() {
        registerInfo = RegisterInfoVO("", "", "", "")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        binding.lifecycleOwner = this
        binding.registerInfo = registerInfo
    }
}