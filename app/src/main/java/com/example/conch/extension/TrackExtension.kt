package com.example.conch.extension

import android.support.v4.media.MediaMetadataCompat
import com.example.conch.data.model.Track

fun Track.toMediaMetadataCompat(): MediaMetadataCompat =
    MediaMetadataCompat.Builder().also {

        it.id = id.toString()
        it.title = title
        it.artist = artist
        it.albumArtUri = coverPath
        it.mediaUri = localPath
        it.displayTitle = title
        it.displaySubtitle = artist
        it.displayDescription = albumName
        it.displayIconUri = coverPath
    }.build()

fun List<Track>.toMediaMetadataCompat(): List<MediaMetadataCompat> =
    this.map { it.toMediaMetadataCompat() }