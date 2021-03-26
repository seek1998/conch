package com.example.conch.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import com.bumptech.glide.Glide
import com.example.conch.data.model.Track
import com.example.conch.data.model.User
import com.example.conch.extension.asAlbumArtContentUri
import com.example.conch.service.MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS
import com.example.conch.service.NOTIFICATION_LARGE_ICON_SIZE
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class PersistentStorage private constructor(val context: Context) {

    private var preferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: PersistentStorage? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: PersistentStorage(context).also { instance = it }
            }
    }


    suspend fun saveLoggedInUser(user: User) {
        val json = gson.toJson(user)

        preferences.edit().putString("logged_in_user", json).apply()
    }

    fun loadLoggedInUser(): User {
        val json = preferences.getString("logged_in_user", "")

        return if (json.isNullOrEmpty()) {
            User()
        } else {
            gson.fromJson(json, User::class.java)
        }
    }

    suspend fun saveRecentPlayList(list: List<Track>) {

        preferences.edit()
            .putInt(RECENT_PLAY_LIST_SIZE, list.size)
            .apply()

        list.forEach {
            val json = gson.toJson(it)
            preferences.edit()
                .putString(RECENT_PLAY_LIST_ITEM + list.indexOf(it), json)
                .apply()
        }
    }

    suspend fun loadRecentPlaylist(): List<Track> {
        val size = preferences.getInt(RECENT_PLAY_LIST_SIZE, 0)

        if (size == 0) return emptyList()

        val list = ArrayList<Track>(20)
        for (index in 1 until size) {
            val json = preferences.getString(RECENT_PLAY_LIST_ITEM + index.toString(), "")
            val item = gson.fromJson(json, Track::class.java)

            if (item == null) {
                Log.i(TAG, "$index is null")
                continue
            }

            list.add(item)
        }
        return list
    }

    suspend fun saveRecentSong(description: MediaDescriptionCompat, position: Long) {

        withContext(Dispatchers.IO) {

            val localIconUri = Glide.with(context).asFile().load(description.iconUri)
                .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE).get()
                .asAlbumArtContentUri()

            preferences.edit()
                .putString(RECENT_SONG_MEDIA_ID_KEY, description.mediaId)
                .putString(RECENT_SONG_TITLE_KEY, description.title.toString())
                .putString(RECENT_SONG_SUBTITLE_KEY, description.subtitle.toString())
                .putString(RECENT_SONG_ICON_URI_KEY, localIconUri.toString())
                .putLong(RECENT_SONG_POSITION_KEY, position)
                .apply()
        }
    }

    fun loadRecentSong(): MediaBrowserCompat.MediaItem? {
        val mediaId = preferences.getString(RECENT_SONG_MEDIA_ID_KEY, null)
        return if (mediaId == null) {
            null
        } else {
            val extras = Bundle().also {
                val position = preferences.getLong(RECENT_SONG_POSITION_KEY, 0L)
                it.putLong(MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS, position)
            }

            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(mediaId)
                    .setTitle(preferences.getString(RECENT_SONG_TITLE_KEY, ""))
                    .setSubtitle(preferences.getString(RECENT_SONG_SUBTITLE_KEY, ""))
                    .setIconUri(Uri.parse(preferences.getString(RECENT_SONG_ICON_URI_KEY, "")))
                    .setExtras(extras)
                    .build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
        }
    }
}

private const val TAG = "PersistentStorage"

private const val PREFERENCES_NAME = "conch"
private const val RECENT_SONG_MEDIA_ID_KEY = "recent_song_media_id"
private const val RECENT_SONG_TITLE_KEY = "recent_song_title"
private const val RECENT_SONG_SUBTITLE_KEY = "recent_song_subtitle"
private const val RECENT_SONG_ICON_URI_KEY = "recent_song_icon_uri"
private const val RECENT_SONG_POSITION_KEY = "recent_song_position"

private const val RECENT_PLAY_LIST_ITEM = "recent_play_list_item"
private const val RECENT_PLAY_LIST_SIZE = "recent_play_list_size"