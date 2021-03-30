package com.example.conch.extension

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat

fun MediaSessionCompat.QueueItem.toMediaMetadataCompat():
        MediaMetadataCompat {

    return MediaMetadataCompat.Builder().also {
        it.id = description.mediaId ?: ""
        it.title = description.title?.toString()
        it.artist = description.subtitle?.toString()
        it.albumArtUri = description.iconUri?.toString()
        it.mediaUri = description.mediaUri?.toString()
        it.displayTitle = description.title?.toString()
        it.displaySubtitle = description.subtitle.toString()
        it.displayDescription = description.mediaDescription.toString()
        it.displayIconUri = description.iconUri?.toString()
    }.build()
}

fun MutableList<MediaSessionCompat.QueueItem>.toMediaMetadataCompat() =
    this.map { it.toMediaMetadataCompat() } as MutableList