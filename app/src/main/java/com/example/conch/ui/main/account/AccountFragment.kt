package com.example.conch.ui.main.account

import android.content.Intent
import com.example.conch.R
import com.example.conch.data.model.Playlist
import com.example.conch.databinding.FragmentAccountBinding
import com.example.conch.ui.BaseFragment
import com.example.conch.ui.login.LoginActivity
import com.example.conch.ui.plalylist.PlaylistActivity

class AccountFragment : BaseFragment<FragmentAccountBinding, AccountViewModel>() {

    override fun processLogic() {

        binding.btnToFavorite.setOnClickListener {
            val playlist = Playlist(id = 1, "我喜欢的音乐", description = "没有描述信息", 0L)

            val intent = Intent(this.activity, PlaylistActivity::class.java).apply {
                putExtra("playlist", playlist)
            }
            startActivity(intent)
        }

        binding.btnAccountLogin.setOnClickListener {
            startActivity(Intent(this.activity, LoginActivity::class.java))
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_account

    override fun getViewModelInstance() =
        AccountViewModel(requireActivity().application)

    override fun getViewModelClass(): Class<AccountViewModel> = AccountViewModel::class.java


}