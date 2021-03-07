package com.example.conch

import android.app.Application
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.db.ConchRoomDatabase
import com.example.conch.data.local.PersistentStorage
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication : Application() {

    private lateinit var trackRepository: TrackRepository

    override fun onCreate() {
        super.onCreate()
        initRepository()

        GlobalScope.launch {
            trackRepository = TrackRepository.getInstance().apply {
                updateDateBase(applicationContext)
            }

            createFavoritePlaylist()
            trackRepository.loadOldRecentPlay()
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

    private fun initRepository() {
        val database = getDatabase()
        val storage = getStorage()
        TrackRepository.init(database, storage)
        UserRepository.init(database)
    }

    /**
     * TODO Android部分
     *
     * TODO 歌单的CURD
     * TODO MainActivity中的控制器的实现
     * TODO 播放队列的更换
     * TODO 歌单播放
     * TODO 重构Track
     * TODO
     */

    /**
     * TODO 服务器部分
     *
     * TODO OSS文件的上传和下载
     * TODO
     */

}
