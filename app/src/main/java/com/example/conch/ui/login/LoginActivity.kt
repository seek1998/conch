package com.example.conch.ui.login

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.example.conch.R
import com.example.conch.data.MyResult
import com.example.conch.data.model.User
import com.example.conch.databinding.ActivityLoginBinding
import com.example.conch.ui.BaseActivity
import com.example.conch.ui.main.MainActivity
import com.example.conch.ui.register.RegisterActivity
import com.example.conch.utils.InjectUtil
import com.example.conch.utils.RegexUtil
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    private lateinit var eventBus: EventBus

    override fun processLogic() {

        initEditText()

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

            if (it is MyResult.Success) {

                val user = it.data!!

                val isChecked = binding.acLoginCheckbox.isChecked

                if (isChecked) {

                    activityScope.launch {

                        viewModel.dataMobility(user)

                        runOnUiThread {
                            updateUiWithUser(user)
                        }
                    }

                } else {
                    updateUiWithUser(user)
                }

            }

            if (it is MyResult.Error) {

                it.exception.message?.let { message ->
                    showLoginFailed(message)
                }

            }
        })

        viewModel.taskResult.observe(this, {
            it?.let {
                if (it && binding.loading.isShown) {
                    binding.loading.hide()
                }
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


    private fun showLoginFailed(message: String) {
        toast(message)
    }

    private fun updateUiWithUser(user: User) {
        Log.d(TAG, "update ui")
        toast("你好， ${user.name}")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun getLayoutId() = R.layout.activity_login

    override fun getViewModelInstance() = InjectUtil.provideLoginViewModel(this)

    override fun getViewModelClass() = LoginViewModel::class.java

}

private const val TAG = "LoginActivity"