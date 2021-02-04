package com.example.conch.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.conch.R
import com.example.conch.data.Result
import com.example.conch.data.model.User
import com.example.conch.databinding.ActivityLoginBinding
import com.example.conch.ui.register.RegisterActivity
import com.example.conch.utils.RegexUtil
import com.jaeger.library.StatusBarUtil

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initToolBar()
        initDataBinding()
        initEditText()

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.btnLogin.apply {
            setOnClickListener {

                val email = binding.editEmail.text.toString()
                val password = binding.editPwd.text.toString()

                if (!RegexUtil.isEmail(email)) {
                    Toast.makeText(this@LoginActivity, R.string.wrong_email, Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                viewModel.login(email, password)
            }
        }

        binding.btnRegister.apply {
            setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }

        viewModel.loginResult.observe(this, {
            val loginResult = it

            if (loginResult is Result.Success) {
                loginResult.data?.let { it1 -> updateUiWithUser(it1) }
            }

            if (loginResult is Result.Error) {
                showLoginFailed(loginResult.exception.message)
            }
        })

    }

    private fun initEditText() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "") {
                    binding.btnLogin.isEnabled = false
                    return
                }

                binding.btnLogin.isEnabled = true
            }
        }

        binding.editEmail.addTextChangedListener(textWatcher)
        binding.editPwd.addTextChangedListener(textWatcher)
    }

    private fun initDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.lifecycleOwner = this
    }

    private fun initToolBar() {
        StatusBarUtil.setLightMode(this)
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.white), 0)
    }

    private fun showLoginFailed(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateUiWithUser(user: User) {
        Toast.makeText(this, "你好， ${user.name}。", Toast.LENGTH_SHORT).show()
    }

}