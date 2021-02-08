package com.example.conch.data

import android.content.Context
import com.example.conch.data.local.LocalTrackSource
import com.example.conch.data.model.Track
import com.example.conch.data.remote.Network

object TrackRepository {

    private var localTrackSource: LocalTrackSource? = null

    suspend fun fetchTrackFromRemote(uid: Long): List<Track> {
        return Network.fetchTracksByUID(uid)
    }

    suspend fun fetchTrackFromLocation(context: Context): List<Track> {
        if (localTrackSource == null) {
            localTrackSource = LocalTrackSource(context.contentResolver)
        }

        return localTrackSource!!.getTracks()
    }
}