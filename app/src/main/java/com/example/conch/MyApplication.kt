package com.example.conch

import android.app.Application
import android.content.Intent
import com.example.conch.service.MusicService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        startService(Intent(this, MusicService::class.java))

        fun getApplication() = this
    }

}

