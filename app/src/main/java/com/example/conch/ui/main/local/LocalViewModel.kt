package com.example.conch.ui.main.local

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.conch.data.TrackRepository
import com.example.conch.data.model.Track
import com.example.conch.extension.id
import com.example.conch.extension.isPlayEnabled
import com.example.conch.extension.isPlaying
import com.example.conch.extension.isPrepared
import com.example.conch.service.MusicServiceConnection
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocalViewModel(
    application: Application,
    private val musicServiceConnection: MusicServiceConnection
) : BaseViewModel(application) {

    val localTracksLiveData = MutableLiveData<List<Track>>()

    fun getLocalTrackList(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val list = TrackRepository.getCachedLocalTracks(context)
            localTracksLiveData.postValue(list)
        }
    }

    fun playTrack(track: Track, pauseAllowed: Boolean = true) {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val transportControls = musicServiceConnection.transportControls

        val isPrepared = musicServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && track.id.toString() == nowPlaying?.id) {
            musicServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying ->
                        if (pauseAllowed) transportControls.pause() else Unit
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(TAG, "Playable item clicked but neither play nor pause are enabled!" +
                                    " (mediaId=${track.id})"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(track.id.toString(), null)
        }
    }
}

private const val TAG = "LocalViewModel"