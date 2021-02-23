package com.example.conch.ui.track

import android.app.Activity
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.example.conch.R
import com.example.conch.extension.*
import com.example.conch.service.EMPTY_PLAYBACK_STATE
import com.example.conch.service.MusicServiceConnection
import com.example.conch.service.NOTHING_PLAYING
import com.example.conch.service.SupportedPlayMode
import kotlin.math.floor

class TrackViewModel constructor(
   musicServiceConnection: MusicServiceConnection,
) : ViewModel() {

    val mediaMetadata: MutableLiveData<NowPlayingMetadata> = MutableLiveData()

    val _nowPlaying = musicServiceConnection.nowPlaying

    val _playMode: LiveData<SupportedPlayMode> = musicServiceConnection.playMode

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE

    private var mediaDuration = 0L

    val mediaButtonRes = MutableLiveData<IntArray>()

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = musicServiceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(playbackState, metadata)
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        onMetadataChanged(it)
        mediaDuration = it.duration

    }

    private val musicServiceConnection = musicServiceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
    }

    private fun updateState(
        playbackState: PlaybackStateCompat,
        mediaMetadata: MediaMetadataCompat
    ) {

        if (mediaMetadata.id.toString() != _nowPlaying.value!!.id) {
            //若歌曲信息没有改变，则不更新NowPlaying
            onMetadataChanged(mediaMetadata)
        }

        mediaButtonRes.postValue(
            when (playbackState.isPlaying) {
                true -> intArrayOf(-R.attr.state_play, R.attr.state_pause) //Set pause
                else -> intArrayOf(R.attr.state_play, -R.attr.state_pause) //Set play
            }
        )
    }

    private fun onMetadataChanged(metadata: MediaMetadataCompat) {

        val nowPlayingMetadata = NowPlayingMetadata(
            id = metadata.id.orEmpty(),
            albumArtUri = metadata.displayIconUri,
            title = metadata.displayTitle?.trim(),
            subtitle = metadata.displaySubtitle?.trim(),
            duration = NowPlayingMetadata.timestampToMSS(metadata.duration),
            _duration = floor(metadata.duration / 1E3).toInt()
        )

        this.mediaMetadata.postValue(nowPlayingMetadata)
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

    fun changePlayMode() {
        musicServiceConnection.changePlayMode(currentMode = _playMode.value)
    }

    fun disconnect(activity: Activity) = musicServiceConnection.disconnect()

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




