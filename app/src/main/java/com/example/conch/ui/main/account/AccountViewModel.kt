package com.example.conch.ui.main.account

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.model.Playlist
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : BaseViewModel(application) {

    private val trackRepository = TrackRepository.getInstance()

    private val userRepository = UserRepository.getInstance()

    private val user = userRepository.loggedInUser

    private val playlists = MutableLiveData<List<Playlist>>().apply {
        emptyList<Playlist>()
    }

    val _playlist: LiveData<List<Playlist>> = playlists

    fun createNewPlaylist(newPlaylist: Playlist) {
        newPlaylist.uid = user.id
        GlobalScope.launch {
            trackRepository.create(newPlaylist)
        }
    }

    fun loadAllPlaylist() {
        GlobalScope.launch {
            val result = trackRepository.getPlaylists(user.id)
            playlists.postValue(result)
        }
    }


}