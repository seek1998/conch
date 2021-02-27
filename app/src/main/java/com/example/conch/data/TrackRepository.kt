package com.example.conch.data

import android.content.Context
import com.example.conch.data.local.LocalTrackSource
import com.example.conch.data.model.Track
import com.example.conch.data.remote.Network

object TrackRepository {

    private var localTrackSource: LocalTrackSource? = null

    private var currentPlaylists: List<Track> = emptyList()

    private var cachedLocalTracks: List<Track> = emptyList()

    val queueTrack = mutableListOf<String>()

    suspend fun getCurrentPlaylist(context: Context): List<Track> {
        if (currentPlaylists.isEmpty()) {
            currentPlaylists = fetchTracksFromLocation(context)
            currentPlaylists.forEach {
                queueTrack.add(it.title)
            }
        }
        return currentPlaylists
    }

    suspend fun getCachedLocalTracks(context: Context, refresh: Boolean = false): List<Track> {

        if (cachedLocalTracks.isEmpty() || refresh)
            cachedLocalTracks = fetchTracksFromLocation(context)

        return cachedLocalTracks
    }

    suspend fun fetchTrackFromRemote(uid: Long): List<Track> {
        return Network.fetchTracksByUID(uid)
    }

    suspend fun fetchTracksFromLocation(context: Context): List<Track> {
        if (localTrackSource == null) {
            localTrackSource = LocalTrackSource(context.contentResolver)
        }

        return localTrackSource!!.getTracks()
    }
}