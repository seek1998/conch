package com.example.conch.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Playlist(
    @field:PrimaryKey(autoGenerate = true) var id: Long = 0,
    var title: String = "",
    var size: Int = NO_TRACK,
    var description: String = "",
    @field:ColumnInfo(defaultValue = "0") var uid: Long = 0
) : Parcelable {
    companion object {
        @Ignore
        const val NO_TRACK = 0

        @Ignore
        const val PLAYLIST_FAVORITE_ID = 1L
    }
}





