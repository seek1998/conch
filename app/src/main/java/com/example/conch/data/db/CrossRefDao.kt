package com.example.conch.data.db

import androidx.room.*

@Dao
interface CrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg crossRef: PlaylistTrackCrossRef)

    // 返回删除的行数
    @Delete
    suspend fun delete(vararg crossRef: PlaylistTrackCrossRef): Int

    @Query("SELECT * FROM playlisttrackcrossref WHERE trackId = :trackId AND playlistId = :playlistId")
    suspend fun find(trackId: Long, playlistId: Long): PlaylistTrackCrossRef?

    @Query("DELETE FROM playlisttrackcrossref WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: Long)

}