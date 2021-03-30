package com.example.conch.data

import android.net.Uri
import android.os.Environment.DIRECTORY_MUSIC
import android.util.Log
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
import com.example.conch.extension.getMediaExt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class TrackRepository private constructor(
    database: ConchRoomDatabase,
    private val storage: PersistentStorage,
    private val localMediaSource: LocalMediaSource,
    private val remoteMediaSource: RemoteMediaSource,
    private val network: Network
) : CoroutineScope by MainScope() {

    init {
        checkFileDir()
    }

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

    suspend fun updateRecentPlay(mediaStoreId: Long) {
        cachedLocalTracks.find {
            it.mediaStoreId == mediaStoreId
        }?.let {
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

    suspend fun getPlaylists(uid: Long): MutableList<Playlist> = playlistDao.getPlaylistByUid(uid)

    suspend fun insertTracks(vararg track: Track) = trackDao.insert(*track)

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.delete(playlist)
        crossRefDao.deleteByPlaylistId(playlist.id)
    }

    suspend fun deleteLocalTrack(track: Track) {
        localMediaSource.deleteTrack(track)
        trackDao.delete(track)
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
        return tracks.sortedBy { track ->
            crossRefs.find { it.trackId == track.mediaStoreId }?.serialNumber
        }.reversed()
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
            it.title == remoteTrack.title &&
                    it.size == remoteTrack.size
        }
    }

    suspend fun fetchTracksFromRemote(uid: Long): MyResult<List<Track>> {
        return network.fetchTracksByUID(uid)
    }

    private suspend fun fetchTracksFromLocation(): MutableList<Track> {
        return localMediaSource.getTracks()
    }

    suspend fun postTrack(track: Track): MyResult<Track> {
        return network.postTrack(track)
    }

    fun uploadTrackFile(
        track: Track,
        uploadProcess: MutableLiveData<IOProgress>
    ) {
        val objectKey = getObjectKey(track, FileType.TRACK)
        val uploadUri = Uri.parse(track.contentUri)
        remoteMediaSource.uploadTrackFile(objectKey, uploadUri, uploadProcess, track)
    }

    fun uploadTrackCover(track: Track) {
        val uploadUri = Uri.parse(track.albumArt)
        val objectKey = getObjectKey(track, FileType.IMG)
        remoteMediaSource.uploadTrackCover(objectKey, uploadUri)
    }

    fun downloadTrackFile(
        track: Track,
        downloadProgress: MutableLiveData<IOProgress>
    ) {
        val objectKey = getObjectKey(track, FileType.TRACK)
        val downloadDir = getTrackDir(track)
        remoteMediaSource.downloadTrackFile(
            objectKey = objectKey,
            downloadDir = downloadDir,
            downloadProgress = downloadProgress,
            track = track
        )
    }

    fun downloadTrackCover(track: Track) {
        val objectKey = getObjectKey(track, FileType.IMG)
        val downloadDir = getTrackDir(track)
        remoteMediaSource.downloadTrackCover(
            objectKey = objectKey,
            downloadDir = downloadDir,
            track = track
        )
    }

    private fun getObjectKey(track: Track, fileType: FileType): String {
        val uid = track.uid
        var ext = ""
        var folderName = ""
        when (fileType) {
            FileType.IMG -> {
                ext = "jpg"
                folderName = "image"
            }
            FileType.TRACK -> {
                ext = track.getMediaExt()!!
                folderName = "track"
            }
        }

        return "$uid/$folderName/${track.id}.$ext"
    }

    private fun getTrackDir(track: Track): File {
        val uid = track.uid
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

    fun checkFileDir(uid: Long = User.LOCAL_USER) {
        val context = MyApplication._context
        context?.run {
            val filesDir = getExternalFilesDir(DIRECTORY_MUSIC)
            val userDir = File(filesDir, uid.toString())
            if (!userDir.exists()) {
                userDir.mkdir()
            }
        }
    }

    @WorkerThread
    suspend fun dataMobility(
        user: User,
        taskResult: MutableLiveData<Boolean>
    ) {
        trackDao.updateAfterLogin(user.id)

        playlistDao.updateAfterLogin(user.id)

        taskResult.postValue(true)
    }

    companion object {

        @Volatile
        private var instance: TrackRepository? = null

        fun init(
            database: ConchRoomDatabase,
            storage: PersistentStorage,
            localMediaSource: LocalMediaSource,
            remoteMediaSource: RemoteMediaSource,
            network: Network
        ) {
            synchronized(this) {
                instance ?: TrackRepository(
                    database,
                    storage,
                    localMediaSource,
                    remoteMediaSource,
                    network
                )
                    .also { instance = it }
            }
        }

        fun getInstance() = instance!!
    }

    enum class FileType {
        TRACK,
        IMG
    }
}

private const val MAX_RECORD_RECENT_PLAY_NUMBER = 20

private const val TAG = "TrackRepository"