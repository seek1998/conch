package com.example.conch.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.conch.data.model.Track

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tracks: List<Track>)

}