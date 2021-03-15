package com.example.conch.ui

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseActivity<VDB : ViewDataBinding, VM : BaseViewModel> : AppCompatActivity() {

    protected lateinit var binding: VDB

    protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return getViewModelInstance() as T
            }
        }).get(getViewModelClass())

        processLogic()
    }

    fun toast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).apply {
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
