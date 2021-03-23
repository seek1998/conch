package com.example.conch.ui.main.cloud

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conch.data.MyResult
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.model.Track
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.launch

class CloudViewModel(application: Application) : BaseViewModel(application) {

    private val userId = UserRepository.getInstance().loggedInUser.id

    private val trackRepository = TrackRepository.getInstance()

    val remoteTracks = MutableLiveData<List<Track>>()

    val fetchRemoteTracksResult = MutableLiveData<MyResult<List<Track>>>()

    fun getDataFromRemote() {
        viewModelScope.launch {
            val result = trackRepository.fetchTracksFromRemote(userId)
            fetchRemoteTracksResult.postValue(result)
        }
    }

    fun refresh() = getDataFromRemote()

}