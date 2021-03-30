package com.example.conch.utils

import android.app.Activity
import android.app.Application
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.service.MusicServiceConnection
import com.example.conch.ui.login.LoginViewModel
import com.example.conch.ui.main.MainViewModel
import com.example.conch.ui.main.RemoteTrackIOViewModel
import com.example.conch.ui.main.cloud.CloudViewModel
import com.example.conch.ui.main.local.LocalViewModel
import com.example.conch.ui.playlist.PlaylistViewModel
import com.example.conch.ui.queue.QueueViewModel
import com.example.conch.ui.track.TrackViewModel
import com.example.conch.ui.user.UserViewModel

object InjectUtil {

    private val userRepository = UserRepository.getInstance()

    private val trackRepository = TrackRepository.getInstance()

    private fun provideMusicServiceConnection(application: Application): MusicServiceConnection {
        return MusicServiceConnection.getInstance(
            application
        )
    }

    fun provideTrackViewModel(activity: Activity): TrackViewModel {
        val application = activity.application
        val musicServiceConnection = provideMusicServiceConnection(application)
        return TrackViewModel.Factory(application, musicServiceConnection, trackRepository)
            .create(TrackViewModel::class.java)
    }

    fun provideLocalViewModel(activity: Activity): LocalViewModel {
        return LocalViewModel(activity.application)
    }

    fun provideMainViewModelFactory(activity: Activity): MainViewModel.Factory {
        val application = activity.application
        val musicServiceConnection = provideMusicServiceConnection(application)
        return MainViewModel.Factory(application, musicServiceConnection)
    }

    fun providePlaylistViewModelFactory(activity: Activity): PlaylistViewModel.Factory {
        val application = activity.application
        val musicServiceConnection = provideMusicServiceConnection(application)
        return PlaylistViewModel.Factory(application, musicServiceConnection)
    }

    fun provideCloudViewModel(activity: Activity): CloudViewModel {
        val application = activity.application
        return CloudViewModel(application)
    }

    fun provideRemoteTrackViewModelFactory(activity: Activity): RemoteTrackIOViewModel.Factory {
        return RemoteTrackIOViewModel.Factory(activity.application)
    }

    fun provideQueueViewModel(activity: Activity): QueueViewModel {
        return QueueViewModel(activity.application, trackRepository)
    }

    fun provideUserViewModel(activity: Activity): UserViewModel {
        val application = activity.application
        return UserViewModel(application, userRepository)
    }

    fun provideLoginViewModel(activity: Activity): LoginViewModel {
        val application = activity.application
        return LoginViewModel(application, userRepository, trackRepository)
    }

}

private const val TAG = "InjectUtil"