package com.example.conch.ui.register

import android.content.Intent
import android.widget.Toast
import com.example.conch.R
import com.example.conch.data.MyResult
import com.example.conch.data.model.RegisterInfoVO
import com.example.conch.databinding.ActivityRegisterBinding
import com.example.conch.ui.BaseActivity
import com.example.conch.ui.login.LoginActivity
import com.example.conch.utils.RegexUtil

class RegisterActivity : BaseActivity<ActivityRegisterBinding, RegisterViewModel>() {

    lateinit var registerInfo: RegisterInfoVO

    override fun processLogic() {
        registerInfo = RegisterInfoVO()

        binding.registerInfo = this.registerInfo

        binding.btnGetCaptcha.apply {
            setOnClickListener {
                val email = registerInfo.email.trim()
                if (!RegexUtil.isEmail(email)) {
                    Toast.makeText(this@RegisterActivity, R.string.wrong_email, Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
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

            if (result is MyResult.Success) {
                sendCaptchaSucceeded()
            }

            if (result is MyResult.Error) {
                showErrorMessage(result.exception.message)
            }
        })

        viewModel.registerResult.observe(this, {
            val result = it

            if (result is MyResult.Success) {
                registerSucceeded()
            }

            if (result is MyResult.Error) {
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

    override fun getLayoutId(): Int = R.layout.activity_register

    override fun getViewModelInstance() = RegisterViewModel(application)

    override fun getViewModelClass(): Class<RegisterViewModel> = RegisterViewModel::class.java
}