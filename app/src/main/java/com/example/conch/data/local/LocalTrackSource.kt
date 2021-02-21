package com.example.conch.data.local

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.conch.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalTrackSource(private val contentResolver: ContentResolver) {
    private val TAG = LocalTrackSource::class.java.simpleName

    suspend fun getTracks(): List<Track> {
        val Tracks = mutableListOf<Track>()
        /**
         * Working with [ContentResolver]s can be slow, so we'll do this off the main
         * thread inside a coroutine.
         */
        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM
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

                Log.i(TAG, "Found ${cursor.count} Tracks")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val album = cursor.getString(albumColumn)
                    val cover = getAlbumCoverPathFromAlbumId(contentResolver, albumId)
                    val contentUri: Uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    val Track = Track(
                        id = id,
                        mediaStoreId = id,
                        title = title,
                        artist = artist,
                        albumId = albumId,
                        albumName = album,
                        coverPath = cover,
                        localUri = contentUri
                    )
                    Tracks += Track
                    Log.v(TAG, "Added Tracks: $Track")
                }
            }
        }

        return Tracks
    }

    private fun getAlbumCoverPathFromAlbumId(
        contentResolver: ContentResolver,
        albumId: Long
    ): String {
        val albumArtUri =
            Uri.parse("content://media/external/audio/albumart")
        val coverUri = ContentUris.withAppendedId(albumArtUri, albumId)
        Log.d(TAG, coverUri.toString())
        return coverUri.toString()

//        return try {
//            val inputStream: InputStream? = contentResolver.openInputStream(coverUri)
//            inputStream?.close()
//            coverUri.toString()
//        } catch (e: IOException) {
//            ""
//        } catch (e: IllegalStateException) {
//            ""
//        }
    }
}