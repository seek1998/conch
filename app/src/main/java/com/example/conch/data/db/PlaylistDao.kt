package com.example.conch.data.db

import androidx.room.*
import com.example.conch.data.model.Playlist

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: Playlist)

    @Query("DELETE FROM playlist WHERE uid = :uid")
    suspend fun deleteByUid(uid: Long)

    @Query("DELETE FROM playlist WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM playlist WHERE uid = :uid")
    suspend fun getPlaylistByUid(uid: Long): List<Playlist>

    @Query("SELECT * FROM playlist WHERE id = :id")
    suspend fun getPlaylistById(id: Long): Playlist

    @Transaction
    @Query("SELECT * FROM Playlist WHERE id = :pid")
    suspend fun getPlaylistWithTracks(pid: Long): List<PlaylistWithTracks>

}