package com.example.conch.ui.main.account

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.model.Playlist
import com.example.conch.ui.BaseViewModel
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : BaseViewModel(application) {

    private val trackRepository = TrackRepository.getInstance()

    private val userRepository = UserRepository.getInstance()

    private val user = userRepository.loggedInUser

    val playlists = MutableLiveData<List<Playlist>>().apply {
        emptyList<Playlist>()
    }

    fun createNewPlaylist(title: String, description: String) {
        val newPlaylist = Playlist(
            title = title,
            size = 0,
            uid = userRepository.loggedInUser.id,
            description = description
        )
        viewModelScope.launch {
            trackRepository.createPlaylist(newPlaylist)
            loadAllPlaylist()
        }
    }

    fun loadAllPlaylist() {
        viewModelScope.launch {
            val result = trackRepository.getPlaylists(user.id)
            playlists.postValue(result)
        }
    }

    fun getFavoritePlaylist(): Playlist {
        return playlists.value!!.first {
            it.id == Playlist.PLAYLIST_FAVORITE_ID
        }
    }

}

private const val TAG = "AccountViewModel"