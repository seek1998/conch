package com.example.conch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob

abstract class BaseFragment<VDB : ViewDataBinding, VM : BaseViewModel> : Fragment(),
    CoroutineScope by MainScope() {

    protected lateinit var binding: VDB

    protected lateinit var viewModel: VM

    protected lateinit var scope: CoroutineScope

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

        scope = CoroutineScope(coroutineContext + SupervisorJob())

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        processLogic()
    }

    fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).apply {
            setText(msg)
            show()
        }
    }

    abstract fun processLogic()

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun getViewModelInstance(): VM

    abstract fun getViewModelClass(): Class<VM>


}