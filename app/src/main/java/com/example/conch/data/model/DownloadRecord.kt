package com.example.conch.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class DownloadRecord(
    @field:PrimaryKey(autoGenerate = false)
    var id: Long = 0L,
    var mediaStoreId: Long = 0L
) : Parcelable