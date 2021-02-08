package com.example.conch.ui.register

import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.conch.R
import com.example.conch.data.Result
import com.example.conch.data.model.RegisterInfoVO
import com.example.conch.databinding.ActivityRegisterBinding
import com.example.conch.ui.BaseActivity
import com.example.conch.ui.login.LoginActivity
import com.jaeger.library.StatusBarUtil

class RegisterActivity : BaseActivity<ActivityRegisterBinding, RegisterViewModel>() {

    lateinit var registerInfo: RegisterInfoVO

    override fun processLogic() {
        registerInfo = RegisterInfoVO()

        binding.registerInfo = this.registerInfo

        initToolBar()

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


    override fun getLayoutId(): Int = R.layout.activity_register

    override fun getViewModelInstance() = RegisterViewModel(application)

    override fun getViewModelClass(): Class<RegisterViewModel> = RegisterViewModel::class.java
}