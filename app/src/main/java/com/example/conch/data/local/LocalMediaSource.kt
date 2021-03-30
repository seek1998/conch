package com.example.conch.data.local

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.conch.data.model.Track
import com.example.conch.extension.getIntValue
import com.example.conch.extension.getLongValue
import com.example.conch.extension.getStringValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

@SuppressLint("InlinedApi")
class LocalMediaSource private constructor(private val contentResolver: ContentResolver) {

    fun deleteTrack(track: Track) {

        try {

            val where = "${MediaStore.Audio.Media._ID} = ?"
            val args = arrayOf(track.mediaStoreId.toString())
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            contentResolver.delete(uri, where, args)

        } catch (ignored: Exception) {

        }
    }

    suspend fun insertTrack(track: Track) {

    }

    suspend fun getTracks(): MutableList<Track> {

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
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATA
            )

            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = null
            val selectionArgs = null
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                Log.i(TAG, "Found ${cursor.count} Tracks")

                while (cursor.moveToNext()) {
                    val id = cursor.getLongValue(MediaStore.Audio.Media._ID)
                    val title = cursor.getStringValue(MediaStore.Audio.Media.TITLE)
                    val artist = cursor.getStringValue(MediaStore.Audio.Media.ARTIST)
                    val albumId = cursor.getLongValue(MediaStore.Audio.Media.ALBUM_ID)
                    val album = cursor.getStringValue(MediaStore.Audio.Media.ALBUM)
                    val duration = cursor.getIntValue(MediaStore.Audio.Media.DURATION)
                    val size = cursor.getLongValue(MediaStore.Audio.Media.SIZE)
                    val cover = getAlbumCoverPathFromAlbumId(contentResolver, albumId)
                    val contentUri: Uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val contentType = cursor.getStringValue(MediaStore.Audio.Media.MIME_TYPE)
                    val path = cursor.getStringValue(MediaStore.Audio.Media.DATA)

                    val item = Track(
                        mediaStoreId = id,
                        title = title,
                        artist = artist,
                        albumId = albumId,
                        albumName = album,
                        albumArt = cover,
                        contentUri = contentUri.toString(),
                        path = path,
                        duration = duration,
                        size = size,
                        type = contentType
                    )

                    tracks.add(item)
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

        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(coverUri)
            inputStream?.close()
            coverUri.toString()
        } catch (ignored: IOException) {
            ""
        } catch (ignored: IllegalStateException) {
            ""
        }
    }

    companion object {

        @Volatile
        private var instance: LocalMediaSource? = null

        fun init(contentResolver: ContentResolver) {
            synchronized(this) {
                instance ?: LocalMediaSource(contentResolver)
                    .also { instance = it }
            }
        }

        fun getInstance() = instance!!
    }
}

private const val TAG = "LocalMediaSource"