package com.example.conch.data.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track


data class TrackWithPlaylists(

    @Embedded
    val track: Track,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            PlaylistTrackCrossRef::class,
            parentColumn = "trackId",
            entityColumn = "playlistId"
        )
    )
    val playlists: List<Playlist>
)