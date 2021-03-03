package com.example.conch

import android.app.Application
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.db.ConchRoomDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initRepository()

        GlobalScope.launch {
            TrackRepository.getInstance().updateDateBase(applicationContext)
        }
    }

    private fun getDatabase(): ConchRoomDatabase = ConchRoomDatabase.getDatabase(this)

    private fun initRepository() {
        val database = getDatabase()
        TrackRepository.init(database)
        UserRepository.init(database)
    }

}
