package com.example.conch.ui.main

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.*
import com.example.conch.data.MyResult
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Playlist.Companion.PLAYLIST_FAVORITE_ID
import com.example.conch.data.model.Track
import com.example.conch.extension.*
import com.example.conch.service.EMPTY_PLAYBACK_STATE
import com.example.conch.service.MusicServiceConnection
import com.example.conch.service.NOTHING_PLAYING
import com.example.conch.ui.BaseViewModel
import com.example.conch.ui.track.NowPlayingMetadata
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.floor

class MainViewModel(
    application: Application,
    musicServiceConnection: MusicServiceConnection
) : BaseViewModel(application) {

    private val trackRepository = TrackRepository.getInstance()

    private val userRepository = UserRepository.getInstance()

    private val handler = Handler(Looper.getMainLooper())

    val isTrackFavorite: MutableLiveData<Boolean> = MutableLiveData()

    val recentPlay = MutableLiveData<List<Track>>()

    val playlists = MutableLiveData<MutableList<Playlist>>()

    val postTrackInfoResult = MutableLiveData<MyResult<Track>>()

    private var updateRecentPlay = true

    val nowPlayingMetadata = MutableLiveData<NowPlayingMetadata>().apply {
        NowPlayingMetadata(title = "标题", subtitle = "作者")
    }

    private val queueTracks: LiveData<MutableList<MediaMetadataCompat>> =
        musicServiceConnection.queueTracks

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE

    val btnPlayPauseLevel = MutableLiveData<Int>()

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = musicServiceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(playbackState, metadata)
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        onMetadataChanged(it)
    }

    private val musicServiceConnection = musicServiceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
    }

    private fun updateState(
        playbackState: PlaybackStateCompat,
        newMetadata: MediaMetadataCompat
    ) {
        if (newMetadata.duration == 0L) return

        //若歌曲没有信息改变，则不更新NowPlayingMetadata
        if (newMetadata.id.toString() != nowPlayingMetadata.value?.id) {
            onMetadataChanged(newMetadata)
            checkRecentPlay()
        }

        btnPlayPauseLevel.postValue(
            when (playbackState.isPlaying) {
                true -> 1 //Set pause
                else -> 0 //Set play
            }
        )
    }

    private fun onMetadataChanged(newMetadata: MediaMetadataCompat) {

        val nowPlayingMetadata = NowPlayingMetadata(
            id = newMetadata.id.orEmpty(),
            albumArtUri = newMetadata.displayIconUri,
            title = newMetadata.displayTitle?.trim(),
            subtitle = newMetadata.displaySubtitle?.trim(),
            duration = NowPlayingMetadata.timestampToMSS(newMetadata.duration),
            _duration = floor(newMetadata.duration / 1E3).toInt()
        )

        this.nowPlayingMetadata.postValue(nowPlayingMetadata)
    }

    fun playOrPause() {
        if (playbackState.isPlaying) {
            musicServiceConnection.transportControls.pause()
        } else {
            musicServiceConnection.transportControls.play()
        }
    }

    fun isLoggedIn() = userRepository.isLoggedIn()

    fun getUser() = userRepository.loggedInUser

    fun getQueueTrack(): MutableList<String> {
        val tracks = mutableListOf<String>()
        val queue = queueTracks.value
        queue!!.forEach {
            tracks.add(it.title!!)
        }
        return tracks
    }

    fun checkRecentPlay(): Boolean = handler.postDelayed({
        val newData = trackRepository.getRecentPlay()

        if (recentPlay.value != newData) {
            recentPlay.postValue(newData)
        }

    }, 1000L)

    fun playTrack(track: Track, pauseAllowed: Boolean = true, extra: Bundle? = null) {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val transportControls = musicServiceConnection.transportControls

        val isPrepared = musicServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && track.mediaStoreId.toString() == nowPlaying?.id) {
            musicServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> {
                        if (pauseAllowed) transportControls.pause() else Unit
                    }

                    playbackState.isPlayEnabled -> {
                        transportControls.play()
                    }
                    else -> {
                        Log.w(
                            TAG, "Playable item clicked but neither play nor pause are enabled!" +
                                    " (mediaId=${track.mediaStoreId})"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(track.mediaStoreId.toString(), extra)
        }
    }

    fun isFavorite(trackId: Long) {
        viewModelScope.launch {
            val result = trackRepository.isFavorite(trackId)
            isTrackFavorite.postValue(result)
        }
    }

    fun changeFavoriteMode(trackId: Long, playlistId: Long = PLAYLIST_FAVORITE_ID) =
        viewModelScope.launch {

            isTrackFavorite.value?.let {
                if (it) {
                    //如果已经在喜欢列表中，则取消喜欢
                    trackRepository.removeTrackFromPlaylist(trackId, playlistId)
                    isTrackFavorite.postValue(false)
                } else {
                    trackRepository.addTrackToPlaylist(trackId, playlistId)
                    isTrackFavorite.postValue(true)
                }

                loadAllPlaylist()
            }
        }

    override fun onCleared() {
        super.onCleared()
        updateRecentPlay = false
        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)
    }

    fun refreshLocalData() {
        viewModelScope.launch {
            trackRepository.getCachedLocalTracks(refresh = true)
        }
    }

    fun addTrackToPlaylist(trackId: Long, playlistId: Long) =
        viewModelScope.launch {
            trackRepository.addTrackToPlaylist(trackId, playlistId)
        }

    fun loadAllPlaylist() {
        viewModelScope.launch {
            val result = trackRepository.getPlaylists(userRepository.loggedInUser.id)
            playlists.postValue(result)
        }
    }

    fun postTrackToCloud(track: Track) =
        viewModelScope.launch {
            track.uid = userRepository.loggedInUser.id
            val result = trackRepository.postTrack(track)
            postTrackInfoResult.postValue(result)
        }

    fun deleteLocalTrack(track: Track) =
        viewModelScope.launch {
            trackRepository.deleteLocalTrack(track)
        }

    suspend fun getPlaylistCover(playlist: Playlist) =
        viewModelScope.async {
            return@async trackRepository.getPlaylistCoverPath(playlist.id)
        }

    class Factory(
        private val application: Application,
        private val musicServiceConnection: MusicServiceConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(application, musicServiceConnection) as T
        }
    }
}

private const val TAG = "MainViewModel"