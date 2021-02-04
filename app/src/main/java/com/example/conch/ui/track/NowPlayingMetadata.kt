package com.example.conch.ui.track

import android.net.Uri
import kotlin.math.floor

data class NowPlayingMetadata(
    var id: String = "",
    var albumArtUri: Uri = Uri.EMPTY,
    var title: String? = "标题",
    var subtitle: String? = "作者",
    var duration: String = "--:--",
    var _duration: Int = 0
) {

    companion object {
        //格式转换
        fun timestampToMSS(position: Long): String {
            val totalSeconds = floor(position / 1E3).toInt()
            val minutes = totalSeconds / 60
            val remainingSeconds = totalSeconds - (minutes * 60)
            return if (position < 0) "--:--"
            else "%d:%02d".format(minutes, remainingSeconds)
        }
    }
}