package com.example.conch.ui.main.account

import android.app.Application
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

    val playlists = MutableLiveData<List<Playlist>>().apply {
        emptyList<Playlist>()
    }

    fun createNewPlaylist(newPlaylist: Playlist) {
        newPlaylist.uid = user.id
        GlobalScope.launch {
            trackRepository.createPlaylist(newPlaylist)
        }
    }

    fun loadAllPlaylist() {
        GlobalScope.launch {
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