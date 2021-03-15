package com.example.conch.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Track(
    //由服务端生成的ID
    @field:PrimaryKey(autoGenerate = false) var id: Long = 0,
    //本地生成的id
    var mediaStoreId: Long = 0,

    var uid: Long = User.LOCAL_USER,

    //歌曲名称
    var title: String = "标题",
    //作者
    var artist: String = "作者",
    //本地存储路径
    @field:Ignore
    val localPath: String = "",
    //时长
    var duration: String = "",
    //单位byte
    var size: Long = 0L,
    //MIME文件类型
    var type: String = "",
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

