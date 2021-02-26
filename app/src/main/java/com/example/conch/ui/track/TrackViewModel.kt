package com.example.conch.ui.track

import android.app.Activity
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.*
import com.example.conch.R
import com.example.conch.data.TrackRepository
import com.example.conch.extension.*
import com.example.conch.service.EMPTY_PLAYBACK_STATE
import com.example.conch.service.MusicServiceConnection
import com.example.conch.service.NOTHING_PLAYING
import com.example.conch.service.SupportedPlayMode
import kotlin.math.floor

class TrackViewModel constructor(
    musicServiceConnection: MusicServiceConnection,
) : ViewModel() {

    //TODO 分离playbackState和nowPlaying的观察

    val nowPlayingMetadata: MutableLiveData<NowPlayingMetadata> =
        MutableLiveData(NowPlayingMetadata())

    val _playMode: LiveData<SupportedPlayMode> = musicServiceConnection.playMode

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE

    val mediaButtonRes = MutableLiveData<IntArray>()

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = musicServiceConnection.nowPlaying.value ?: NOTHING_PLAYING

        if (playbackState.isPlaying || playbackState.isPlayEnabled){
            updateState(playbackState, metadata)
        }
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        Log.d(TAG, "监测到数据变化， 新id:${it.id}")
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

        if (newMetadata.id.toString() != nowPlayingMetadata.value?.id) {
            onMetadataChanged(newMetadata)//若歌曲没有信息改变，则不更新NowPlayingMetadata
        }

        mediaButtonRes.postValue(
            when (playbackState.isPlaying) {
                true -> intArrayOf(-R.attr.state_play, R.attr.state_pause) //Set pause
                else -> intArrayOf(R.attr.state_play, -R.attr.state_pause) //Set play
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
        musicServiceConnection.changePlayMode(currentMode = _playMode.value)

    fun disconnect(activity: Activity) = musicServiceConnection.disconnect()

    fun getQueueTrack() = TrackRepository.queueTrack

    override fun onCleared() {
        super.onCleared()

        // Remove the permanent observers from the MusicServiceConnection.
        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)

        // Stop updating the position
    }

    class Factory(
        private val musicServiceConnection: MusicServiceConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TrackViewModel(musicServiceConnection) as T
        }
    }

    private val TAG = this::class.java.simpleName
}




