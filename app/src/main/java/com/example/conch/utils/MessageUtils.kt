package com.example.conch.utils

import java.io.Serializable

object MessageUtils {

    data class MediaScanMessage(val mediaPath: String = "", val mimeType: String = "") :
        Serializable
}