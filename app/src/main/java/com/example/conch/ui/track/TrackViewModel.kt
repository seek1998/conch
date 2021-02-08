package com.example.conch.ui.track

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.conch.R
import com.example.conch.extension.*
import com.example.conch.service.EMPTY_PLAYBACK_STATE
import com.example.conch.service.MusicServiceConnection
import com.example.conch.service.NOTHING_PLAYING

class TrackViewModel constructor(
    musicServiceConnection: MusicServiceConnection,
) : ViewModel() {

    val mediaMetadata: MutableLiveData<NowPlayingMetadata> = MutableLiveData()

    val _nowPlaying = musicServiceConnection.nowPlaying

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE

    private var mediaDuration = 0L

    val mediaButtonRes = MutableLiveData<IntArray>()

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = musicServiceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(playbackState, metadata)
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        updateState(playbackState, it)
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
        onMetadataChanged(mediaMetadata)

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
            _duration = metadata.duration.toInt()
        )

        this.mediaMetadata.postValue(nowPlayingMetadata)
    }

    fun skipToNext() = musicServiceConnection.transportControls.skipToNext()

    fun skipToPrevious() = musicServiceConnection.transportControls.skipToPrevious()

    fun playOrPause() {
        if (playbackState.isPlaying) {
            musicServiceConnection.transportControls.pause()
        } else {
            musicServiceConnection.transportControls.play()
        }
    }

    fun changeTrackProgress(newProgress: Int) {
        musicServiceConnection.transportControls.seekTo(newProgress.toLong())
    }


    class Factory(
        private val musicServiceConnection: MusicServiceConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TrackViewModel(musicServiceConnection) as T
        }
    }
}

