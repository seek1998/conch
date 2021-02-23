package com.example.conch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseFragment<VDB : ViewDataBinding, VM : BaseViewModel> : Fragment() {

    protected lateinit var binding: VDB
    protected lateinit var viewModel: VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return getViewModelInstance() as T
            }
        })[getViewModelClass()]
        processLogic()
        return binding.root
    }


    abstract fun processLogic()

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun getViewModelInstance(): VM

    abstract fun getViewModelClass(): Class<VM>



}