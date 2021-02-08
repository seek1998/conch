package com.example.conch.ui.main.account

import com.example.conch.R
import com.example.conch.databinding.FragmentAccountBinding
import com.example.conch.ui.BaseFragment

class AccountFragment : BaseFragment<FragmentAccountBinding, AccountViewModel>() {

    override fun processLogic() {

    }

    override fun getLayoutId(): Int = R.layout.fragment_account

    override fun getViewModelInstance() =
        AccountViewModel(application = requireActivity().application)

    override fun getViewModelClass(): Class<AccountViewModel> = AccountViewModel::class.java


}