package com.example.conch.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class User(

    /**
     * 当uid为0时，为未登录的本地用户
     */

    @field:PrimaryKey(autoGenerate = false) var id: Long = LOCAL_USER,
    var email: String = "",
    var name: String = "",
    var password: String = ""
){
    companion object {
        @Ignore const val LOCAL_USER = 0L
    }
}