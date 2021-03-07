package com.example.conch.data

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.conch.data.db.ConchRoomDatabase
import com.example.conch.data.db.PlaylistTrackCrossRef
import com.example.conch.data.local.LocalMediaSource
import com.example.conch.data.local.PersistentStorage
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.data.remote.Network
import java.util.*
import kotlin.collections.ArrayList

class TrackRepository private constructor(
    private val database: ConchRoomDatabase,
    private val storage: PersistentStorage
) {

    private val recentPlay: Queue<Track> = LinkedList()

    private val playlistDao = database.playlistDao()

    private val trackDao = database.trackDao()

    private val crossRefDao = database.crossRefDao()

    private var localMediaSource: LocalMediaSource? = null

    private var cachedLocalTracks: List<Track> = emptyList()

    //当前播放队队列
    var currentQueueTracks: List<Track> = emptyList()

    //
    private suspend fun saveRecentPlay() {
        val MAX_RECORD_NUMBER = 20

        val list = recentPlay.toList()
        if (list.size >= MAX_RECORD_NUMBER) {
            storage.saveRecentPlayList(list.subList(0, 20))
        } else {
            storage.saveRecentPlayList(list)
        }

    }

    suspend fun updateRecentPlay(trackId: Long) {

        trackDao.getTrack(trackId)?.let {
            recentPlay.remove(it)
            recentPlay.offer(it)
            saveRecentPlay()
        }
    }

    suspend fun loadOldRecentPlay() {
        if (recentPlay.isEmpty()) {
            storage.loadRecentPlaylist().let {
                if (it.isEmpty()) {
                    return@let
                } else {
                    Log.i(TAG, "old recent play list: $it")
                    it.asReversed().forEach { recentPlay.offer(it) }
                }
            }
        }
    }


    fun getRecentPlay(): ArrayList<Track> {
        var recentPlay = ArrayList<Track>().apply {
            //越近的记录, index越小
            addAll(recentPlay.toList().asReversed())
        }
        return recentPlay
    }

    suspend fun isFavorite(trackId: Long): Boolean {
        val result = crossRefDao.find(trackId = trackId, playlistId = Playlist.PLAYLIST_FAVORITE_ID)
        return result != null
    }

    suspend fun getPlaylistCoverPath(id: Long): String {
        val tracks = getTracksByPlaylistId(id)

        if (tracks.isEmpty()) {
            return ""
        }

        //返回第一个有封面的歌曲的封面，作为歌单封面
        tracks.forEach {
            if (it.coverPath.isNotEmpty()) {
                return it.coverPath
            }
        }

        return ""
    }

    suspend fun getPlaylists(uid: Long): List<Playlist> {
        val fakedata = Playlist(id = 2L, "电子音乐", 0, "我喜欢的", 0L)
        playlistDao.insert(fakedata)
        return playlistDao.getPlaylistByUid(uid)
    }

    suspend fun insertTracks(vararg track: Track) = trackDao.insert(*track)

    suspend fun getPlaylistById(id: Long) = playlistDao.getPlaylistById(id)

    suspend fun updatePlaylist(playlist: Playlist) = playlistDao.update(playlist)

    suspend fun deletePlaylist(playlist: Playlist) = playlistDao.delete(playlist)

    suspend fun createPlaylist(playlist: Playlist) = playlistDao.insert(playlist)

    suspend fun addTrackToPlaylist(trackId: Long, playlistId: Long) {

        val newCrossRef = PlaylistTrackCrossRef(trackId = trackId, playlistId = playlistId)

        playlistDao.getPlaylistById(playlistId)?.apply {
            size++
        }?.let {
            playlistDao.update(it)
        }

        crossRefDao.insert(newCrossRef)
    }

    suspend fun removeTrackFromPlaylist(trackId: Long, playlistId: Long) {
        val newCrossRef = PlaylistTrackCrossRef(trackId = trackId, playlistId = playlistId)

        playlistDao.getPlaylistById(playlistId)?.apply {
            size--
        }?.let {
            playlistDao.update(it)
        }

        crossRefDao.delete(newCrossRef)
    }

//    suspend fun insertFakeData() {
//        val fakedata = listOf(
//            Track(
//                id = 44807,
//                mediaStoreId = 44807,
//                uid = 0,
//                title = "Starfall",
//                artist = "袁娅维",
//                localPath = "content://media/external/audio/media/44807",
//                duration = 0,
//                albumId = 16,
//                albumName = "Starfall",
//                coverPath = "content://media/external/audio/albumart/16",
//                RemotePath = ""
//            ),
//
//            Track(
//                id = 44799,
//                mediaStoreId = 44799,
//                uid = 0,
//                title = "M18",
//                artist = "梶浦由記",
//                localPath = "content://media/external/audio/media/44799",
//                duration = 0,
//                albumId = 15,
//                albumName = "空の境界 Vol.2 殺人考察(前)",
//                coverPath = "content://media/external/audio/albumart/15",
//                RemotePath = ""
//            ),
//
//            Track(
//                id = 44792,
//                mediaStoreId = 44792,
//                uid = 0,
//                title = "Rubia",
//                artist = "多多poi",
//                localPath = "content://media/external/audio/media/44792",
//                duration = 0,
//                albumId = 14,
//                albumName = "多多翻唱",
//                coverPath = "",
//                RemotePath = ""
//            )
//        )
//
//        val list = Playlist(id = 1, "我喜欢的音乐", description = "没有描述信息", size = 3)
//        val corssRef1 = PlaylistTrackCrossRef(playlistId = 1, trackId = 44792)
//        val corssRef2 = PlaylistTrackCrossRef(playlistId = 1, trackId = 44807)
//        val corssRef3 = PlaylistTrackCrossRef(playlistId = 1, trackId = 44799)
//        playlistDao.insert(list)
//        trackDao.insert(fakedata)
//        crossRefDao.insert(corssRef1)
//        crossRefDao.insert(corssRef2)
//        crossRefDao.insert(corssRef3)
//    }

    @WorkerThread
    suspend fun getTracksByPlaylistId(playlistId: Long): List<Track> {

        val playlistWithTracks = playlistDao.getPlaylistWithTracks(playlistId)

        Log.i(TAG, "Found playlist [$playlistId]: $playlistWithTracks")

        playlistWithTracks?.let {
            Log.d(TAG, playlistWithTracks.tracks.toString())
            return playlistWithTracks.tracks
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
        if (localMediaSource == null) {
            localMediaSource = LocalMediaSource(context.contentResolver)
        }

        return localMediaSource!!.getTracks()
    }

    suspend fun updateDateBase(context: Context) {
        getCachedLocalTracks(context, true).forEach {
            trackDao.insert(it)
        }
    }


    companion object {

        @Volatile
        private var instance: TrackRepository? = null

        fun init(database: ConchRoomDatabase, storage: PersistentStorage) {
            synchronized(this) {
                instance ?: TrackRepository(database, storage)
                    .also { instance = it }
            }
        }

        fun getInstance() = instance!!
    }
}

private const val TAG = "TrackRepository"