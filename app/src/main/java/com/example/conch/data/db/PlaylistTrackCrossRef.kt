package com.example.conch.data.db

import androidx.room.Entity

@Entity(
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrackCrossRef(
    val playlistId: Long,
    val trackId: Long
)