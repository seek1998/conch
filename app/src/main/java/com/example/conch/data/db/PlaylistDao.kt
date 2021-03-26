package com.example.conch.data.db

import androidx.room.*
import com.example.conch.data.model.Playlist

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(playlist: Playlist)

    @Update
    suspend fun update(playlist: Playlist)

    @Delete
    suspend fun delete(playlist: Playlist)

    @Query("DELETE FROM playlist WHERE uid = :uid")
    suspend fun deleteByUid(uid: Long)

    @Query("DELETE FROM playlist WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM playlist WHERE uid = :uid")
    suspend fun getPlaylistByUid(uid: Long): MutableList<Playlist>

    @Query("SELECT * FROM playlist WHERE id = :id")
    suspend fun getPlaylistById(id: Long): Playlist?

    @Transaction
    @Query("SELECT * FROM Playlist WHERE id = :playlistId")
    suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTracks?

}