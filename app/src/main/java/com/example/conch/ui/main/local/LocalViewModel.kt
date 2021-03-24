package com.example.conch.ui.main.local

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conch.data.TrackRepository
import com.example.conch.data.model.Track
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalViewModel(application: Application) : BaseViewModel(application) {

    private val trackRepository = TrackRepository.getInstance()

    val localTracksLiveData = MutableLiveData<List<Track>>()

    fun getLocalTracks() = viewModelScope.launch {
        val list = trackRepository.getCachedLocalTracks()
        localTracksLiveData.postValue(list)
    }

    suspend fun refreshLocalData() = with(Dispatchers.IO) {
        trackRepository.getCachedLocalTracks(true)
        getLocalTracks()
    }
}

private const val TAG = "LocalViewModel"