package com.example.conch.data

import android.content.Context
import android.net.Uri
import android.os.Environment.DIRECTORY_MUSIC
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.example.conch.MyApplication
import com.example.conch.data.db.ConchRoomDatabase
import com.example.conch.data.db.PlaylistTrackCrossRef
import com.example.conch.data.dto.IOProgress
import com.example.conch.data.local.LocalMediaSource
import com.example.conch.data.local.PersistentStorage
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.data.model.User
import com.example.conch.data.remote.Network
import com.example.conch.data.remote.RemoteMediaSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class TrackRepository private constructor(
    database: ConchRoomDatabase,
    private val storage: PersistentStorage,
    private val localMediaSource: LocalMediaSource,
    private val remoteMediaSource: RemoteMediaSource
) : CoroutineScope by MainScope() {

    init {
        checkFileDir()
    }

    private val network = Network.getInstance()

    private val recentPlay: Queue<Track> = LinkedList()

    private val playlistDao = database.playlistDao()

    private val trackDao = database.trackDao()

    private val crossRefDao = database.crossRefDao()

    private var cachedLocalTracks: MutableList<Track> = ArrayList()

    val currentQueueTracks: MutableList<Track> = ArrayList()

    suspend fun checkCurrentQueueTracks() {
        if (currentQueueTracks.isEmpty()) {
            val list = getCachedLocalTracks()
            currentQueueTracks.addAll(list)
        }
    }

    fun updateCurrentQueueTracks(newList: List<Track>) =
        currentQueueTracks.apply {
            clear()
            addAll(newList)
        }


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
        storage.loadRecentPlaylist().let { old ->
            if (old.isEmpty()) {
                return@let
            } else {
                Log.i(TAG, "old recent play list: $old")
                old.asReversed().onEach { recentPlay.offer(it) }
            }
        }
    }

    fun getRecentPlay(): MutableList<Track> {
        return ArrayList<Track>().apply {
            //越近的记录, index越小
            addAll(recentPlay.toMutableList().asReversed())
        }
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
        //返回歌单中第一首有封面的歌曲的封面，作为歌单封面
        return tracks.find {
            it.albumArt.isNotEmpty()
        }?.albumArt ?: ""
    }

    suspend fun getPlaylists(uid: Long): List<Playlist> = playlistDao.getPlaylistByUid(uid)

    suspend fun insertTracks(vararg track: Track) = trackDao.insert(*track)

    suspend fun getPlaylistById(playlistId: Long) = playlistDao.getPlaylistById(playlistId)

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.delete(playlist)
        crossRefDao.deleteByPlaylistId(playlist.id)
    }

    suspend fun createPlaylist(playlist: Playlist) = playlistDao.insert(playlist)

    suspend fun addTrackToPlaylist(mediaStoreId: Long, playlistId: Long) {

        cachedLocalTracks.find { it.mediaStoreId == mediaStoreId }?.let {
            trackDao.insert(it)
        }

        playlistDao.getPlaylistById(playlistId)?.apply {
            this.size++
        }?.let {
            val newCrossRef = PlaylistTrackCrossRef(
                trackId = mediaStoreId,
                playlistId = playlistId,
                serialNumber = it.size
            )
            crossRefDao.insert(newCrossRef)
            playlistDao.update(it)
        }
    }

    suspend fun removeTrackFromPlaylist(trackId: Long, playlistId: Long) {

        playlistDao.getPlaylistById(playlistId)?.apply {
            this.size--
        }?.let {
            playlistDao.update(it)
            val deletedCrossRef = PlaylistTrackCrossRef(trackId = trackId, playlistId = playlistId)
            crossRefDao.delete(deletedCrossRef)
        }
    }

    @WorkerThread
    suspend fun getTracksByPlaylistId(playlistId: Long): List<Track> {
        val tracks = playlistDao.getPlaylistWithTracks(playlistId)?.tracks ?: return emptyList()

        val crossRefs = mutableListOf<PlaylistTrackCrossRef>()
        tracks.onEach {
            crossRefs.add(crossRefDao.find(it.mediaStoreId, playlistId)!!)
        }

        //根据加入歌单的顺序排序，越新的记录越靠前
        return tracks.sortedBy { track -> crossRefs.find { it.trackId == track.mediaStoreId }?.serialNumber }
            .reversed()
    }

    suspend fun getTrackByMediaStoreId(mediaStoreId: Long): Track? {
        return cachedLocalTracks.find { it.mediaStoreId == mediaStoreId }
    }

    suspend fun getCachedLocalTracks(refresh: Boolean = false): List<Track> {

        if (cachedLocalTracks.isEmpty() || refresh)
            cachedLocalTracks = fetchTracksFromLocation()

        return cachedLocalTracks
    }

    fun isRemoteTrackLocal(remoteTrack: Track): Boolean {
        return cachedLocalTracks.any {
            it.uid == remoteTrack.uid &&
                    it.title == remoteTrack.title &&
                    it.size == remoteTrack.size
        }
    }

    suspend fun fetchTracksFromRemote(uid: Long): MyResult<List<Track>> {
        return network.fetchTracksByUID(uid)
    }

    suspend fun fetchTracksFromLocation(): MutableList<Track> {
        return localMediaSource.getTracks()
    }

    suspend fun updateDateBase(context: Context) {

    }

    suspend fun postTrack(track: Track): MyResult<Track> {
        return network.postTrack(track)
    }

    @WorkerThread
    suspend fun uploadTrackFile(
        track: Track,
        uid: Long,
        uploadProcess: MutableLiveData<IOProgress>
    ) {
        val uploadUri = Uri.parse(track.contentUri)
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(track.type)
        val folderName = "track"
        val uploadKey = "$uid/$folderName/${track.id}.$ext"
        remoteMediaSource.uploadTrackFile(uploadKey, uploadUri, uploadProcess, track)
    }

    @WorkerThread
    suspend fun uploadTrackCover(track: Track, uid: Long) {
        val uploadUri = Uri.parse(track.albumArt)
        val ext = "jpg"
        val folderName = "image"
        val uploadKey = "$uid/$folderName/${track.id}.$ext"
        remoteMediaSource.uploadTrackCover(uploadKey, uploadUri)
    }

    @WorkerThread
    suspend fun downloadTrackFile(track: Track) {
        val uid = track.uid
        val downloadDir = getTrackDir(uid, track)
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(track.type)
        val folderName = "track"
        val downloadKey = "$uid/$folderName/${track.id}.$ext"
        remoteMediaSource.downloadTrackFile(
            objectKey = downloadKey,
            downloadDir = downloadDir,
            track = track
        )
    }

    @WorkerThread
    suspend fun downloadTrackCover(track: Track) {
        val uid = track.uid
        val downloadDir = getTrackDir(uid, track)
        val ext = "jpg"
        val folderName = "image"
        val downloadKey = "$uid/$folderName/${track.id}.$ext"
        remoteMediaSource.downloadTrackCover(
            objectKey = downloadKey,
            downloadDir = downloadDir,
            track = track
        )
    }

    suspend fun deleteLocalTrack(track: Track) {
        Log.d(TAG, track.toString())
        localMediaSource.deleteTrack(track)
        trackDao.delete(track)
    }

    private fun getTrackDir(uid: Long = User.LOCAL_USER, track: Track): File {
        val context = MyApplication._context!!
        context.run {
            val filesDir = getExternalFilesDir(DIRECTORY_MUSIC)
            val userDir = File(filesDir, uid.toString())
            //注意，这里使用的id是服务端生成的id
            val trackDir = File(userDir, track.id.toString())
            if (!trackDir.exists()) {
                trackDir.mkdir()
            }
            return trackDir
        }
    }

    private fun checkFileDir(uid: Long = User.LOCAL_USER) {
        val context = MyApplication._context
        context?.run {
            val filesDir = getExternalFilesDir(DIRECTORY_MUSIC)
            val userDir = File(filesDir, uid.toString())
            if (!userDir.exists()) {
                userDir.mkdir()
            }
        }
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