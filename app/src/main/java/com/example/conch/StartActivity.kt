package com.example.conch

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.db.ConchRoomDatabase
import com.example.conch.data.local.LocalMediaSource
import com.example.conch.data.local.PersistentStorage
import com.example.conch.data.remote.ConchOss
import com.example.conch.data.remote.Network
import com.example.conch.data.remote.RemoteMediaSource
import com.example.conch.ui.main.MainActivity
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    private lateinit var trackRepository: TrackRepository

    private lateinit var localMediaSource: LocalMediaSource

    private lateinit var remoteMediaSource: RemoteMediaSource

    private lateinit var network: Network

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        checkPermission()
    }

    private fun checkPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
                Manifest.permission.CALL_PHONE
            )
            .request { allGranted, grantedList, deniedList ->

                if (allGranted) {
                    Log.i(TAG, "All permissions are granted")
                }

                this.initialize()
            }

    }

    private fun initialize() = scope.launch {

        initOSSClient()
        initMediaSource()
        initRepository()

        trackRepository = TrackRepository.getInstance().apply {
            loadOldRecentPlay()
        }

        startActivity(
            Intent(this@StartActivity, MainActivity::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this@StartActivity).toBundle()
        )
    }

    private fun getDatabase(): ConchRoomDatabase = ConchRoomDatabase.getDatabase(applicationContext)

    private fun getStorage(): PersistentStorage = PersistentStorage.getInstance(applicationContext)

    private fun initOSSClient() = ConchOss.init(applicationContext)

    private fun initMediaSource() {
        val oss = ConchOss.getInstance()
        RemoteMediaSource.init(oss)
        this.remoteMediaSource = RemoteMediaSource.getInstance()
        LocalMediaSource.init(application.contentResolver)
        this.localMediaSource = LocalMediaSource.getInstance()
    }

    private fun initRepository() {
        val database = getDatabase()
        val storage = getStorage()
        val network = Network.getInstance()
        TrackRepository.init(database, storage, localMediaSource, remoteMediaSource, network)
        UserRepository.init(database, storage, network)
    }
}

private const val TAG = "StartActivity"