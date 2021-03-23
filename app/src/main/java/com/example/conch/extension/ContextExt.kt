package com.example.conch.extension

import android.content.Context
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Handler
import android.os.Looper

private val artworkUri = Uri.parse("content://media/external/audio/albumart")

fun Context.rescanAndDeletePath(path: String, callback: () -> Unit) {

    val scanFileHandler = Handler(Looper.getMainLooper())
    scanFileHandler.postDelayed({
        callback()
    }, SCAN_FILE_MAX_DURATION)

    MediaScannerConnection.scanFile(applicationContext, arrayOf(path), null) { path, uri ->
        scanFileHandler.removeCallbacksAndMessages(null)
        try {
            applicationContext.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
        }
        callback()
    }
}

/**
 * 判断是否有网络连接
 * @return
 */
private fun Context.isNetworkConnected(): Boolean {
    val manager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    manager.activeNetworkInfo?.let {
        return it.isAvailable
    }

    return false
}

private const val SCAN_FILE_MAX_DURATION = 1000L