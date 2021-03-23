package com.example.conch.extension

import android.support.v4.media.MediaMetadataCompat
import android.webkit.MimeTypeMap
import com.example.conch.data.model.Track

fun Track.toMediaMetadataCompat(): MediaMetadataCompat =
    MediaMetadataCompat.Builder().also {

        it.id = mediaStoreId.toString()
        it.title = title
        it.artist = artist
        it.albumArtUri = albumArt
        it.mediaUri = contentUri
        it.displayTitle = title
        it.displaySubtitle = artist
        it.displayDescription = albumName
        it.displayIconUri = albumArt
    }.build()

fun List<Track>.toMediaMetadataCompat(): List<MediaMetadataCompat> =
    this.map { it.toMediaMetadataCompat() }

fun Track.getMediaExt(): String? {
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
}

fun Track.getMediaFileName(): String {
    return "${title}.${getMediaExt()}"
}

