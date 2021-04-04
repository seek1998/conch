package com.example.conch.data.db

import androidx.room.*
import com.example.conch.data.model.Track

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg track: Track)

    @Update
    suspend fun update(track: Track)

    @Query("SELECT * FROM track WHERE uid = :uid")
    suspend fun getTracksByUid(uid: Long): List<Track>

    @Transaction
    @Query("SELECT * FROM Track WHERE mediaStoreId = :mediaStoreId")
    suspend fun getPlaylistWithTracks(mediaStoreId: Long): List<TrackWithPlaylists>

    @Query("SELECT * FROM track WHERE mediaStoreId = :mediaStoreId")
    suspend fun getTrack(mediaStoreId: Long): Track?

    @Query("DELETE FROM track")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(track: Track)

    @Query("UPDATE track SET id = :id WHERE mediaStoreId = :mediaStoreId")
    suspend fun checkId(id: Long, mediaStoreId: Long)

    @Query("UPDATE track SET id = :id WHERE mediaStoreId = 0")
    suspend fun setId(id: Long)

}