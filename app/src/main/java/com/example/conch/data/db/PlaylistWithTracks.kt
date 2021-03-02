package com.example.conch.data.db

import androidx.room.Embedded
import androidx.room.Relation
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track

data class PlaylistWithTracks(
    @field:Embedded
    val playlist: Playlist,

    @Relation(
        parentColumn = "id",
        entityColumn = "pid"
    )
    val tracks: List<Track>
)


