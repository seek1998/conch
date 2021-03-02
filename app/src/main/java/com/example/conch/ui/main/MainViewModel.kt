package com.example.conch.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.model.Track
import com.example.conch.extension.id
import com.example.conch.extension.isPlayEnabled
import com.example.conch.extension.isPlaying
import com.example.conch.extension.isPrepared
import com.example.conch.service.MusicServiceConnection
import com.example.conch.ui.BaseViewModel

class MainViewModel(
    application: Application,
    private val musicServiceConnection: MusicServiceConnection
) : BaseViewModel(application) {

    private val trackRepository = TrackRepository.getInstance()

    private val userRepository = UserRepository.getInstance()

    fun playTrack(track: Track, pauseAllowed: Boolean = true) {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val transportControls = musicServiceConnection.transportControls

        val isPrepared = musicServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && track.id.toString() == nowPlaying?.id) {
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
                                    " (mediaId=${track.id})"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(track.id.toString(), null)
        }
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