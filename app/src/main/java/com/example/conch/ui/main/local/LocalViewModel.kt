package com.example.conch.ui.main.local

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.conch.data.TrackRepository
import com.example.conch.data.model.Track
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocalViewModel(application: Application) : BaseViewModel(application) {

    val localTracksLiveData = MutableLiveData<List<Track>>()

    fun getLocalTrackList(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val list = TrackRepository.fetchTrackFromLocation(context)
            localTracksLiveData.postValue(list)
        }

    }
}