package com.example.conch.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Track(
    var id: Long = 0L, //由服务端生成的ID
    @field:PrimaryKey(autoGenerate = false)
    @Expose(serialize = false, deserialize = false)
    var mediaStoreId: Long = 0L,
    var uid: Long = User.LOCAL_USER,
    var title: String = "",//歌曲名称
    var artist: String = "",//作者
    @field:Ignore
    @Expose(serialize = false, deserialize = false)
    var contentUri: String = "",
    @Expose(serialize = false, deserialize = false)
    var path: String = "",//本地存储路径
    var duration: Int = 0,//时长(单位毫秒)
    var size: Long = 0L,//文件大小（单位byte）
    var type: String = "",//MIME文件类型
    @Expose(serialize = false, deserialize = false) //专辑ID
    var albumId: Long = 0L,
    var albumName: String = "",//专辑名称
    @Expose(serialize = false, deserialize = false)
    var albumArt: String = "",//本地封面路径
) : Parcelable, Comparable<Track> {

    /**
     * 云端数据只和云端数据比较,本地同理
     */
    override fun compareTo(other: Track): Int {
        return if (other.id != 0L && this.id != 0L) {
            (this.id - other.id).toInt()
        } else {
            (this.mediaStoreId - other.mediaStoreId).toInt()
        }
    }
}

