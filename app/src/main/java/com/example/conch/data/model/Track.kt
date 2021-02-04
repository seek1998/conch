package com.example.conch.data.model

import android.net.Uri
import com.google.gson.annotations.Expose

data class Track(
    //由服务端生成的ID
    var id: Long = 0,
    //本地Room生成的id
    @Expose
    var mediaStoreId: Long = 0,
    //歌曲名称
    var title: String = "标题",
    //作者
    var artist: String = "作者",
    //本地存储路径
    @Expose
    val localUri: Uri = Uri.EMPTY,
    //时长
    var duration: Int = 0,
    //专辑ID
    var albumId: Long = 0,
    //专辑名称
    var albumName: String = "",
    //专辑封面
    var coverPath: String = "",
    //云端路径
    @Expose
    var RemoteUrl: Uri = Uri.EMPTY

)
