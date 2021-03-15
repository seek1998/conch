package com.example.conch

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.db.ConchRoomDatabase
import com.example.conch.data.local.LocalMediaSource
import com.example.conch.data.local.PersistentStorage
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.User
import com.example.conch.data.remote.ConchOss
import com.example.conch.data.remote.RemoteMediaSource
import com.example.conch.ui.main.MainActivity
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var trackRepository: TrackRepository

    private lateinit var localMediaSource: LocalMediaSource

    private lateinit var remoteMediaSource: RemoteMediaSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        initPermission()

        launch {

            initOSSClient()
            initMediaSource()
            initRepository()

            trackRepository = TrackRepository.getInstance().apply {
                updateDateBase(applicationContext)
            }

            createFavoritePlaylist()
            trackRepository.loadOldRecentPlay()

            startActivity(Intent(this@StartActivity, MainActivity::class.java))
        }
    }

    private fun initPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.CALL_PHONE
            )
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    Log.i(TAG, "All permissions are granted")
                } else {
                    Log.i(TAG, "These permissions are denied: $deniedList")
                }
            }

    }

    private suspend fun createFavoritePlaylist() {
        trackRepository.getPlaylistById(1L).let {
            if (it == null) {
                trackRepository.createPlaylist(
                    Playlist(
                        1,
                        "我喜欢的音乐",
                        size = Playlist.NO_TRACK,
                        "无描述信息",
                        User.LOCAL_USER
                    )
                )
            }
        }
    }

    private fun getDatabase(): ConchRoomDatabase = ConchRoomDatabase.getDatabase(applicationContext)

    private fun getStorage(): PersistentStorage = PersistentStorage.getInstance(applicationContext)

    private fun initOSSClient() = ConchOss.init(applicationContext)

    private fun initMediaSource() {
        RemoteMediaSource.init(ConchOss.getInstance())
        this.remoteMediaSource = RemoteMediaSource.getInstance()
        localMediaSource = LocalMediaSource(applicationContext.contentResolver)
    }

    private fun initRepository() {
        val database = getDatabase()
        val storage = getStorage()
        TrackRepository.init(database, storage, localMediaSource, remoteMediaSource)
        UserRepository.init(database)
    }
}

private const val TAG = "StartActivity"