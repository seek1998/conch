package com.example.conch.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class User(
    @field:PrimaryKey(autoGenerate = false) var id: Long = LOCAL_USER,
    var email: String = "",
    var name: String = "",
    var password: String = ""
) : Parcelable {
    companion object {
        @Ignore
        const val LOCAL_USER = 0L
    }
}