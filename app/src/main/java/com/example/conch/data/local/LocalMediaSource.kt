package com.example.conch.data.local

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.conch.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

class LocalMediaSource(private val contentResolver: ContentResolver) {


    suspend fun deleteTrack(track: Track) {
        val uri = Uri.parse(track.localPath)
        contentResolver.delete(uri, null, null)
    }

    suspend fun getTracks(): List<Track> {
        val tracks = mutableListOf<Track>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE
            )
            val selection = null
            val selectionArgs = null
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val audioDuration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val fileSize = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val fileType = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                Log.i(TAG, "Found ${cursor.count} Tracks")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val album = cursor.getString(albumColumn)
                    val duration = cursor.getString(audioDuration)
                    val size = cursor.getLong(fileSize)
                    val cover = getAlbumCoverPathFromAlbumId(contentResolver, albumId)
                    val contentUri: Uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val contentType = cursor.getString(fileType)

                    val item = Track(
                        id = id,
                        mediaStoreId = id,
                        title = title,
                        artist = artist,
                        albumId = albumId,
                        albumName = album,
                        coverPath = cover,
                        localPath = contentUri.toString(),
                        duration = duration,
                        size = size,
                        type = contentType
                    )

                    tracks += item
                    Log.v(TAG, "Added Tracks: $item")
                }
            }
        }

        return tracks
    }

    private fun getAlbumCoverPathFromAlbumId(
        contentResolver: ContentResolver,
        albumId: Long
    ): String {
        val albumArtUri =
            Uri.parse("content://media/external/audio/albumart")
        val coverUri = ContentUris.withAppendedId(albumArtUri, albumId)
        Log.d(TAG, coverUri.toString())

        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(coverUri)
            inputStream?.close()
            coverUri.toString()
        } catch (e: IOException) {
            ""
        } catch (e: IllegalStateException) {
            ""
        }
    }
}

private const val TAG = "LocalMediaSource"