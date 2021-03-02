package com.example.conch

import android.app.Application
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.db.ConchRoomDatabase

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initRepository()

    }

    private fun getDatabase(): ConchRoomDatabase = ConchRoomDatabase.getDatabase(this)

    private fun initRepository() {
        val database = getDatabase()
        TrackRepository.init(database)
        UserRepository.init(database)
    }

}
