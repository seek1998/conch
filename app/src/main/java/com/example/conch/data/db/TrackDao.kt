package com.example.conch.data.db

import androidx.room.*
import com.example.conch.data.model.Track

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg track: Track)

    @Transaction
    @Query("SELECT * FROM Track WHERE id = :trackId")
    suspend fun getPlaylistWithTracks(trackId: Long): List<TrackWithPlaylists>

    @Query("SELECT * FROM track WHERE id = :trackId")
    suspend fun getTrack(trackId: Long): Track?

}