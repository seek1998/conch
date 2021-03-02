package com.example.conch.ui.main.local

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.conch.data.TrackRepository
import com.example.conch.data.model.Track
import com.example.conch.service.MusicServiceConnection
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocalViewModel(
    application: Application,
    private val musicServiceConnection: MusicServiceConnection
) : BaseViewModel(application) {

    private val trackRepository = TrackRepository.getInstance()

    val localTracksLiveData = MutableLiveData<List<Track>>()

    fun getLocalTrackList(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val list = trackRepository.getCachedLocalTracks(context)
            localTracksLiveData.postValue(list)
        }
    }

}

private const val TAG = "LocalViewModel"