package com.example.conch.ui.user

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import com.example.conch.R
import com.example.conch.data.MyResult
import com.example.conch.data.model.User
import com.example.conch.databinding.ActivityUserBinding
import com.example.conch.service.MessageEvent
import com.example.conch.service.MessageType
import com.example.conch.ui.BaseActivity
import com.example.conch.ui.main.MainActivity
import com.example.conch.utils.InjectUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class UserActivity : BaseActivity<ActivityUserBinding, UserViewModel>() {

    override fun processLogic() {

        val user = intent.getParcelableExtra("user") ?: User()

        setUpUser(user)

        binding.acUserExit.apply {
            setOnClickListener { confirmIfExit() }
        }

        viewModel.updateResult.observe(this, {
            it?.let {

                if (it is MyResult.Success) {

                    val user = it.data!!

                    setUpUser(user)

                    toast("个人信息已更新")

                    val message =
                        MessageEvent(MessageType.ACTION_UPDATE_USER_INFO).put(user)
                    EventBus.getDefault().postSticky(message)

                }

                if (it is MyResult.Error) {
                    it.exception.message?.let { message ->
                        toast(message)
                    }
                }
            }
        })

        binding.acUserUpdatePassword.apply {
            setOnClickListener { updatePassword(user) }
        }

        binding.acUserSaveName.apply {
            setOnClickListener {
                val newName = binding.acUserName.text.toString()
                updateName(newName, user)
            }
        }
    }

    private fun setUpUser(user: User) = with(binding) {

        acUserId.text = user.id.toString()

        acUserEmail.text = user.email

        acUserName.apply {
            setText(user.name)
            addTextChangedListener(nameWatcher)
        }

        acUserOldPassword.apply {
            addTextChangedListener(passwordWatcher)
        }

        acUserNewPassword.apply {
            addTextChangedListener(passwordWatcher)
        }

        acUserConfirmPassword.apply {
            addTextChangedListener(passwordWatcher)
        }
    }

    private fun updateName(newName: String, oldUser: User) {

        val newUserInfo = User(
            id = oldUser.id,
            email = oldUser.email,
            name = newName,
            password = oldUser.password
        )

        activityScope.launch {
            viewModel.updateUser(newUserInfo)
        }
    }

    private fun updatePassword(oldUser: User) {

        val oldPassword = binding.acUserOldPassword.text.toString()

        val newPassword = binding.acUserNewPassword.text.toString()

        val newUserInfo = User(
            id = oldUser.id,
            email = oldUser.email,
            name = oldUser.name,
            password = newPassword
        )

        activityScope.launch {
            viewModel.updatePassword(oldPassword, newUserInfo)
        }

    }

    private val nameWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(name: Editable?) {

            val warn = binding.acUserNameWarn

            binding.acUserSaveName.visibility = View.GONE

            if (name.toString().trim().isEmpty()) {
                warn.text = "不能为空"
                return
            }

            if (name.toString().contains(" ")) {
                warn.text = "不能含有空格"
                return
            }

            warn.apply {
                setTextColor(ContextCompat.getColor(this@UserActivity, R.color.ok))
                text = "OK"
                binding.acUserSaveName.visibility = View.VISIBLE
            }
        }
    }

    private val passwordWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        @SuppressLint("SetTextI18n")
        override fun afterTextChanged(s: Editable?) {

            val warn = binding.acUserPasswordWarn

            binding.acUserUpdatePassword.visibility = View.GONE

            val oldPassword = binding.acUserOldPassword.text.toString().trim()

            val newPassword = binding.acUserNewPassword.text.toString().trim()

            val confirmPassword = binding.acUserConfirmPassword.text.toString().trim()

            if (newPassword.length < 8) {
                warn.text = getString(R.string.hint_password_too_short)
                return
            }

            if (newPassword != confirmPassword) {
                warn.text = getString(R.string.hint_password_confirm_failed)
                return
            }

            if (oldPassword.isEmpty()) {
                warn.text = getString(R.string.hint_empty_old_password)
                return
            }

            warn.apply {
                setTextColor(ContextCompat.getColor(this@UserActivity, R.color.ok))
                text = "OK"
                binding.acUserUpdatePassword.visibility = View.VISIBLE
            }
        }
    }

    private fun confirmIfExit() {

        MaterialAlertDialogBuilder(this)
            .setTitle("账户")
            .setMessage("确定退出登录吗")
            .setNegativeButton("否", null)
            .setPositiveButton("是") { _, _ ->
                exitLogin()
            }
            .show()
    }

    private fun exitLogin() {

        activityScope.launch {
            viewModel.exitLogin()
        }

        val intent = Intent(this, MainActivity::class.java)

        startActivity(intent)

        finish()
    }


    override fun getLayoutId() = R.layout.activity_user

    override fun getViewModelInstance() = InjectUtil.provideUserViewModel(this)

    override fun getViewModelClass() = UserViewModel::class.java

}