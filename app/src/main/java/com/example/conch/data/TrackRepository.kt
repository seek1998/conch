package com.example.conch.data

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.conch.data.db.ConchRoomDatabase
import com.example.conch.data.local.LocalTrackSource
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.data.remote.Network

class TrackRepository(database: ConchRoomDatabase) {

    private val playlistDao = database.playlistDao()

    private val trackDao = database.trackDao()

    private var localTrackSource: LocalTrackSource? = null

    private var cachedLocalTracks: List<Track> = emptyList()

    var queueTracks: List<MediaMetadataCompat> = emptyList()

    @WorkerThread
    suspend fun getPlaylists(uid: Long): List<Playlist> {
        return playlistDao.getPlaylistByUid(uid)
    }

    @WorkerThread
    suspend fun savePlaylist(playlist: Playlist) = playlistDao.insert(playlist)

    suspend fun insertFakeData(){
        val fakedata = listOf(
            Track(
                id = 44807,
                mediaStoreId = 44807,
                uid = 0,
                pid = 1,
                title = "Starfall",
                artist = "袁娅维",
                localPath = "content://media/external/audio/media/44807",
                duration = 0,
                albumId = 16,
                albumName = "Starfall",
                coverPath = "content://media/external/audio/albumart/16",
                RemotePath = ""
            ),

            Track(
                id = 44799,
                mediaStoreId = 44799,
                uid = 0,
                pid = 1,
                title = "M18",
                artist = "梶浦由記",
                localPath = "content://media/external/audio/media/44799",
                duration = 0,
                albumId = 15,
                albumName = "空の境界 Vol.2 殺人考察(前)",
                coverPath = "content://media/external/audio/albumart/15",
                RemotePath = ""
            ),

            Track(
                id = 44792,
                mediaStoreId = 44792,
                uid = 0,
                pid = 1,
                title = "Rubia",
                artist = "多多poi",
                localPath = "content://media/external/audio/media/44792",
                duration = 0,
                albumId = 14,
                albumName = "多多翻唱",
                coverPath = "",
                RemotePath = ""
            )
        )

        val list = Playlist(id = 1, "我喜欢的音乐", description = "没有描述信息", 0L)
        playlistDao.insert(list)
        trackDao.insert(fakedata)

    }

    @WorkerThread
    suspend fun getTracksByPlayListId(pid: Long): List<Track> {

        val playlistWithTracks = playlistDao.getPlaylistWithTracks(pid)
        Log.d(TAG, pid.toString() + " " +  playlistWithTracks.toString())

        if (playlistWithTracks.isEmpty()) {
            return emptyList()
        }

        playlistWithTracks.forEach {
            if (it.playlist.id == pid) {
                return it.tracks
            }
        }

        return emptyList()
    }

    suspend fun getCachedLocalTracks(context: Context, refresh: Boolean = false): List<Track> {

        if (cachedLocalTracks.isEmpty() || refresh)
            cachedLocalTracks = fetchTracksFromLocation(context)

        return cachedLocalTracks
    }

    suspend fun fetchTracksFromRemote(uid: Long): List<Track> {
        return Network.fetchTracksByUID(uid)
    }

    suspend fun fetchTracksFromLocation(context: Context): List<Track> {
        if (localTrackSource == null) {
            localTrackSource = LocalTrackSource(context.contentResolver)
        }

        return localTrackSource!!.getTracks()
    }

    suspend fun create(playlist: Playlist) {
        playlistDao.insert(playlist)
    }

    companion object {

        @Volatile
        private var instance: TrackRepository? = null

        fun init(database: ConchRoomDatabase) {
            synchronized(this) {
                instance ?: TrackRepository(database)
                    .also { instance = it }
            }
        }

        fun getInstance() = instance!!
    }
}

private const val TAG = "TrackRepository"