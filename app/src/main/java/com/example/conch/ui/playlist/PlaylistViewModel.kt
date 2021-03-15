package com.example.conch.ui.playlist

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.conch.data.TrackRepository
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.extension.id
import com.example.conch.extension.isPlayEnabled
import com.example.conch.extension.isPlaying
import com.example.conch.extension.isPrepared
import com.example.conch.service.MusicServiceConnection
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.launch

class PlaylistViewModel(
    application: Application,
    private val musicServiceConnection: MusicServiceConnection
) : BaseViewModel(application) {

    private val trackRepository = TrackRepository.getInstance()

    val playlistLiveData = MutableLiveData<Playlist>()

    val coverPathLiveData = MutableLiveData<String>()

    val tracksLiveData = MutableLiveData<List<Track>>()

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


    fun getPlaylistTracks(id: Long) {
        viewModelScope.launch {
            val result = trackRepository.getTracksByPlaylistId(playlistId = id)
            Log.d(TAG, result.toString())
            tracksLiveData.postValue(result)
        }
    }

    fun getPlaylistCover(id: Long) {
        viewModelScope.launch {
            val result = trackRepository.getPlaylistCoverPath(id)
            coverPathLiveData.postValue(result)
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            playlistLiveData.value?.let {
                trackRepository.deletePlaylist(it)
            }
        }
    }

    class Factory(
        private val application: Application,
        private val musicServiceConnection: MusicServiceConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlaylistViewModel(application, musicServiceConnection) as T
        }
    }
}

private const val TAG = "PlaylistViewModel"