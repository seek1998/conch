package com.example.conch.ui.main.cloud

import com.example.conch.R
import com.example.conch.databinding.FragmentCloudBinding
import com.example.conch.ui.BaseFragment
import com.example.conch.utils.InjectUtil

class CloudFragment : BaseFragment<FragmentCloudBinding, CloudViewModel>() {

    override fun processLogic() {

    }

    override fun getLayoutId() = R.layout.fragment_cloud

    override fun getViewModelInstance() = InjectUtil.provideCloudViewModel(requireActivity())

    override fun getViewModelClass() = CloudViewModel::class.java
}