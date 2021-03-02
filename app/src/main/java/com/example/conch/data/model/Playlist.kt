package com.example.conch.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Playlist(
    @field:PrimaryKey(autoGenerate = true) var id: Long = 0,
    var title: String = "",
    var description: String = "",
    @field:ColumnInfo(defaultValue = "0") var uid: Long = 0
) : Parcelable





