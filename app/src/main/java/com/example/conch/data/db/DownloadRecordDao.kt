package com.example.conch.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conch.data.model.DownloadRecord

@Dao
interface DownloadRecordDao {

    @Query("SELECT  * FROM downloadrecord WHERE mediaStoreId = :mediaStoreId")
    suspend fun getDownloadRecord(mediaStoreId: Long): DownloadRecord

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg downloadRecord: DownloadRecord)
}