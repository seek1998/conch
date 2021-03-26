package com.example.conch.ui.queue

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.conch.data.TrackRepository
import com.example.conch.data.model.Track
import com.example.conch.ui.BaseViewModel

class QueueViewModel(
    application: Application,
    private val trackRepository: TrackRepository
) : BaseViewModel(application) {

    val tracks = MutableLiveData<MutableList<Track>>()

    fun loadQueue() {
        val queue = trackRepository.currentQueueTracks
        tracks.postValue(queue)
    }

}