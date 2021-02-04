package com.example.conch.utils

import android.content.Context
import com.example.conch.service.MusicServiceConnection
import com.example.conch.ui.track.TrackViewModel

object InjectUtil {

    private fun provideMusicServiceConnection(context: Context): MusicServiceConnection {
        return MusicServiceConnection.getInstance(
            context
        )
    }

    fun provideTrackViewModel(context: Context): TrackViewModel {
        val musicServiceConnection = provideMusicServiceConnection(context)
        return TrackViewModel.Factory(musicServiceConnection).create(TrackViewModel::class.java)
    }

}