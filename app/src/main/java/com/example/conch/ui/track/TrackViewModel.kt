package com.example.conch.ui.track

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.*
import com.example.conch.R
import com.example.conch.data.TrackRepository
import com.example.conch.data.model.Playlist
import com.example.conch.extension.*
import com.example.conch.service.EMPTY_PLAYBACK_STATE
import com.example.conch.service.MusicServiceConnection
import com.example.conch.service.NOTHING_PLAYING
import com.example.conch.service.SupportedPlayMode
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.launch
import kotlin.math.floor

class TrackViewModel constructor(
    musicServiceConnection: MusicServiceConnection, application: Application,
) : BaseViewModel(application) {

    private var trackRepository = TrackRepository.getInstance()

    val nowPlayingMetadata: MutableLiveData<NowPlayingMetadata> = MutableLiveData()

    val isFavorite: MutableLiveData<Boolean> = MutableLiveData(false)

    val playMode: LiveData<SupportedPlayMode> = musicServiceConnection.playMode

    val queueTracks: LiveData<List<MediaMetadataCompat>> = musicServiceConnection.queueTracks

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE

    val mediaButtonRes = MutableLiveData<IntArray>()

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE

        val metadata = musicServiceConnection.nowPlaying.value ?: NOTHING_PLAYING

        updateState(playbackState, metadata)
    }

    val mediaPosition = MutableLiveData<Long>().apply {
        postValue(0L)
    }

    private var updatePosition = true
    private val handler = Handler(Looper.getMainLooper())

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        Log.d(TAG, "监测到数据变化， 新id:${it.id}")
        onMetadataChanged(it)
    }

    private val musicServiceConnection = musicServiceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
        checkPlaybackPosition()
    }


    private fun checkPlaybackPosition(): Boolean = handler.postDelayed({
        val currentPosition = playbackState.currentPlayBackPosition
        if (mediaPosition.value != currentPosition)
            mediaPosition.postValue(currentPosition)
        if (updatePosition)
            checkPlaybackPosition()
    }, 100L)

    private fun updateState(
        playbackState: PlaybackStateCompat,
        newMetadata: MediaMetadataCompat
    ) {
        if (newMetadata.duration == 0L) return

        //若歌曲没有信息改变，则不更新NowPlayingMetadata
        if (newMetadata.id.toString() != nowPlayingMetadata.value?.id) {
            onMetadataChanged(newMetadata)
        }

        mediaButtonRes.postValue(
            when (playbackState.isPlaying) {
                true -> intArrayOf(-R.attr.state_play, R.attr.state_pause) //Set pause
                else -> intArrayOf(R.attr.state_play, -R.attr.state_pause) //Set play
            }


        )
    }

    private fun onMetadataChanged(newMetadata: MediaMetadataCompat) {

        viewModelScope.launch {

            val trackId = newMetadata.id.orEmpty()
            if (trackId.isNotEmpty()){
                val result = trackRepository.isFavorite(trackId.toLong())
                isFavorite.postValue(result)
            }
        }

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

    fun skipToNext() = musicServiceConnection.transportControls.skipToNext()

    fun skipToPrevious() = musicServiceConnection.transportControls.skipToPrevious()

    fun changeTrackProgress(progress: Int) =
        musicServiceConnection.transportControls.seekTo(progress * 1E3.toLong())

    fun playOrPause() {
        if (playbackState.isPlaying) {
            musicServiceConnection.transportControls.pause()
        } else {
            musicServiceConnection.transportControls.play()
        }
    }

    fun changePlayMode() =
        musicServiceConnection.changePlayMode(currentMode = playMode.value)

    fun getQueueTrack(): MutableList<String> {
        val tracks = mutableListOf<String>()
        val queue = queueTracks.value
        queue!!.forEach {
            tracks.add(it.title!!)
        }

        return tracks
    }


    override fun onCleared() {
        super.onCleared()

        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)

        // 停止加载进度
        updatePosition = false
    }

    fun changeFavoriteMode() =
        viewModelScope.launch {
            if (isFavorite.value!!) {
                //如果已经在喜欢列表中，则取消喜欢
                trackRepository.removeTrackFromPlaylist(
                    trackId = nowPlayingMetadata.value!!.id.toLong(),
                    playlistId = Playlist.PLAYLIST_FAVORITE_ID
                )

                isFavorite.postValue(false)
            } else {
                trackRepository.addTrackToPlaylist(
                    trackId = nowPlayingMetadata.value!!.id.toLong(),
                    playlistId = Playlist.PLAYLIST_FAVORITE_ID
                )
                isFavorite.postValue(true)
            }
        }


    class Factory(
        private val musicServiceConnection: MusicServiceConnection,
        private val application: Application
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TrackViewModel(musicServiceConnection, application) as T
        }
    }
}

private const val TAG = "TrackViewModel"
