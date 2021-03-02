package com.example.conch.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import com.example.conch.service.MusicServiceConnection
import com.example.conch.ui.main.MainViewModel
import com.example.conch.ui.main.local.LocalViewModel
import com.example.conch.ui.plalylist.PlaylistViewModel
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
        val musicServiceConnection = provideMusicServiceConnection(activity)
        return LocalViewModel(activity.application, musicServiceConnection)
    }

    fun provideMainViewModelFactory(context: Context): MainViewModel.Factory {
        val applicationContext = context.applicationContext
        val musicServiceConnection = provideMusicServiceConnection(applicationContext)
        return MainViewModel.Factory(applicationContext as Application, musicServiceConnection)
    }

    fun providePlaylistViewModelFactory(context: Context): PlaylistViewModel.Factory {
        val applicationContext = context.applicationContext
        val musicServiceConnection = provideMusicServiceConnection(applicationContext)
        return PlaylistViewModel.Factory(applicationContext as Application, musicServiceConnection)
    }

}