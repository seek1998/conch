package com.example.conch.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

@Entity(
    primaryKeys = ["mediaStoreId", "pid"]
)
@Parcelize
data class Track(
    //由服务端生成的ID
    var id: Long = 0,
    //本地生成的id
    var mediaStoreId: Long = 0,

    var uid: Long = 0,

    var pid: Long = 0,
    //歌曲名称
    var title: String = "标题",
    //作者
    var artist: String = "作者",
    //本地存储路径
    @field:Ignore
    val localPath: String = "",
    //时长
    var duration: Int = 0,
    //专辑ID
    var albumId: Long = 0,
    //专辑名称
    var albumName: String = "",

    //本地封面路径
    var coverPath: String = "",
    //云端路径
    @field:Ignore
    val RemotePath: String = ""
) : Parcelable
