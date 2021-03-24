package com.example.conch.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import com.example.conch.service.MusicServiceConnection
import com.example.conch.ui.main.MainViewModel
import com.example.conch.ui.main.RemoteTrackIOViewModel
import com.example.conch.ui.main.cloud.CloudViewModel
import com.example.conch.ui.main.local.LocalViewModel
import com.example.conch.ui.playlist.PlaylistViewModel
import com.example.conch.ui.track.TrackViewModel

object InjectUtil {

    private fun provideMusicServiceConnection(context: Context): MusicServiceConnection {
        return MusicServiceConnection.getInstance(
            context
        )
    }

    fun provideTrackViewModel(activity: Activity): TrackViewModel {
        val musicServiceConnection = provideMusicServiceConnection(activity)
        return TrackViewModel.Factory(musicServiceConnection, activity.application)
            .create(TrackViewModel::class.java)
    }

    fun provideLocalViewModel(activity: Activity): LocalViewModel {
        return LocalViewModel(activity.application)
    }

    fun provideMainViewModelFactory(activity: Activity): MainViewModel.Factory {

        val musicServiceConnection = provideMusicServiceConnection(activity)
        return MainViewModel.Factory(activity.application, musicServiceConnection)
    }

    fun providePlaylistViewModelFactory(context: Context): PlaylistViewModel.Factory {
        val applicationContext = context.applicationContext
        val musicServiceConnection = provideMusicServiceConnection(applicationContext)
        return PlaylistViewModel.Factory(applicationContext as Application, musicServiceConnection)
    }

    fun provideCloudViewModel(activity: Activity): CloudViewModel {
        val application = activity.application
        return CloudViewModel(application)
    }

    fun provideRemoteTrackViewModelFactory(activity: Activity): RemoteTrackIOViewModel.Factory {
        return RemoteTrackIOViewModel.Factory(activity.application)
    }

}

private const val TAG = "InjectUtil"