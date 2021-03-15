package com.example.conch.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.example.conch.data.db.ConchRoomDatabase
import com.example.conch.data.db.PlaylistTrackCrossRef
import com.example.conch.data.local.LocalMediaSource
import com.example.conch.data.local.PersistentStorage
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.data.remote.Network
import com.example.conch.data.remote.RemoteMediaSource
import java.util.*
import kotlin.collections.ArrayList

class TrackRepository private constructor(
    private val database: ConchRoomDatabase,
    private val storage: PersistentStorage,
    private val localMediaSource: LocalMediaSource,
    private val remoteMediaSource: RemoteMediaSource
) {

    private val network = Network.getInstance()

    private val recentPlay: Queue<Track> = LinkedList()

    private val playlistDao = database.playlistDao()

    private val trackDao = database.trackDao()

    private val crossRefDao = database.crossRefDao()

    private var cachedLocalTracks: List<Track> = emptyList()

    //当前播放队队列
    var currentQueueTracks: List<Track> = emptyList()

    private suspend fun saveRecentPlay() {

        val list = recentPlay.toList()
        if (list.size >= MAX_RECORD_RECENT_PLAY_NUMBER) {
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

    suspend fun getPlaylists(uid: Long): List<Playlist> = playlistDao.getPlaylistByUid(uid)

    suspend fun insertTracks(vararg track: Track) = trackDao.insert(*track)

    suspend fun getPlaylistById(id: Long) = playlistDao.getPlaylistById(id)

    suspend fun updatePlaylist(playlist: Playlist) = playlistDao.update(playlist)

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.delete(playlist)
        crossRefDao.deleteByPlaylistId(playlist.id)
    }

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

    suspend fun getTrack(id: Long) = trackDao.getTrack(id)

    suspend fun getCachedLocalTracks(context: Context, refresh: Boolean = false): List<Track> {

        if (cachedLocalTracks.isEmpty() || refresh)
            cachedLocalTracks = fetchTracksFromLocation(context)

        return cachedLocalTracks
    }

    suspend fun fetchTracksFromRemote(uid: Long): List<Track> {
        return emptyList()
    }

    suspend fun fetchTracksFromLocation(context: Context): List<Track> {
        return localMediaSource.getTracks()
    }

    suspend fun updateDateBase(context: Context) {
        getCachedLocalTracks(context, true).forEach {
            trackDao.insert(it)
        }
    }

    fun uploadTrackFile(track: Track, uid: Long, uploadProcess: MutableLiveData<Int>) {

        val uploadUri = Uri.parse(track.localPath)
        val type = MimeTypeMap.getSingleton().getExtensionFromMimeType(track.type)
        val folderName = "track"
        val uploadKey = "$uid/$folderName/${track.title}.$type"
        remoteMediaSource.uploadTrackFile(uploadKey, uploadUri, uploadProcess)
    }

    fun uploadTrackCover(track: Track, uid: Long) {
        val uploadUri = Uri.parse(track.coverPath)
        val type = ".jpg"
        val folderName = "image"
        val uploadKey = "$uid/$folderName/${track.id}.$type"
        remoteMediaSource.uploadTrackCover(uploadKey, uploadUri)
    }

    companion object {

        @Volatile
        private var instance: TrackRepository? = null

        fun init(
            database: ConchRoomDatabase,
            storage: PersistentStorage,
            localMediaSource: LocalMediaSource,
            remoteMediaSource: RemoteMediaSource
        ) {
            synchronized(this) {
                instance ?: TrackRepository(database, storage, localMediaSource, remoteMediaSource)
                    .also { instance = it }
            }
        }

        fun getInstance() = instance!!
    }

}

private const val MAX_RECORD_RECENT_PLAY_NUMBER = 20

private const val TAG = "TrackRepository"