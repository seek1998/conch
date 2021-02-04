package com.example.conch.ui

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    abstract fun attach(savedInstanceState: Bundle? = null)

    override fun onCleared() {
        super.onCleared()
    }
}