package com.example.conch.ui.track

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.floor

@Parcelize
data class NowPlayingMetadata(
    var id: String = "-1",
    var albumArtUri: Uri = Uri.EMPTY,
    var title: String? = "标题",
    var subtitle: String? = "作者",
    var duration: String = "--:--",//界面展示用值
    var _duration: Int = 0
) : Parcelable {

    companion object {
        //格式转换
        fun timestampToMSS(position: Long): String {
            val totalSeconds = floor(position / 1E3).toInt()
            val minutes = totalSeconds / 60
            val remainingSeconds = totalSeconds - (minutes * 60)
            return if (position < 0) "--:--"
            else "%02d:%02d".format(minutes, remainingSeconds)
        }
    }
}