package com.example.conch.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.conch.extension.id

private const val TAG = "MusicServiceConnection"


class MusicServiceConnection(context: Context) {

    val isConnected = MutableLiveData<Boolean>()
        .apply { postValue(false) }
    val playbackState = MutableLiveData<PlaybackStateCompat>()
        .apply { postValue(EMPTY_PLAYBACK_STATE) }
    val nowPlaying = MutableLiveData<MediaMetadataCompat>()
        .apply { postValue(NOTHING_PLAYING) }

    //随机模式
    val shuffleModeState = MutableLiveData<Int>()
        .apply { postValue(PlaybackStateCompat.SHUFFLE_MODE_ALL) }

    //列表循环,默认模式
    val repeatModeState = MutableLiveData<Int>()
        .apply { postValue(PlaybackStateCompat.REPEAT_MODE_ALL) }

    //单曲循环
    val playMode = MutableLiveData<SupportedPlayMode>().apply {
        postValue(SupportedPlayMode.REPEAT)
    }


    lateinit var mediaController: MediaControllerCompat

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MusicService::class.java),
        mediaBrowserConnectionCallback, null
    ).apply { connect() }


    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        /**
         * Invoked after [MediaBrowserCompat.connect] when the request has successfully
         * completed.
         */
        override fun onConnected() {
            Log.d(TAG, "MediaBrowserConnectionCallback onConnected")
            // Get a MediaController for the MediaSession.
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            isConnected.postValue(true)
            //准备歌曲资源
            // transportControls.prepare()

        }

        /**
         * Invoked when the client is disconnected from the media browser.
         */
        override fun onConnectionSuspended() {
            Log.d(TAG, "MediaBrowserConnectionCallback onConnectionSuspended")
            isConnected.postValue(false)
        }

        /**
         * Invoked when the connection to the media browser failed.
         */
        override fun onConnectionFailed() {
            Log.d(TAG, "MediaBrowserConnectionCallback onConnectionFailed")
            isConnected.postValue(false)
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Log.d(TAG, "MediaControllerCallback onPlaybackStateChanged:$state")
            val playbackStateCompat = state ?: EMPTY_PLAYBACK_STATE
            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
            // sendWidgetPlaybackStateIntent(playbackStateCompat)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.d(TAG, "MediaControllerCallback onMetadataChanged:$metadata")
            nowPlaying.postValue(
                if (metadata?.id == null) {
                    NOTHING_PLAYING
                } else {
                    //sendWidgetMetadataIntent(metadata)
                    metadata
                }
            )
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            super.onShuffleModeChanged(shuffleMode)
            Log.d(TAG, "MediaControllerCallback onShuffleModeChanged:$shuffleMode")
            shuffleModeState.postValue(shuffleMode)

            playMode.postValue(SupportedPlayMode.SHUFFLE)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            super.onRepeatModeChanged(repeatMode)
            Log.d(TAG, "MediaControllerCallback onRepeatModeChanged:$repeatMode")
            repeatModeState.postValue(repeatMode)

            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                playMode.postValue(SupportedPlayMode.REPEAT)
            } else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE){
                playMode.postValue(SupportedPlayMode.REPEAT_ONE)
            }

        }
    }


//    private fun sendWidgetPlaybackStateIntent(playbackStateCompat: PlaybackStateCompat) {
//        val intent = Intent(context, MusicAppWidget::class.java).apply {
//            action = WidgetConstants.STATE_CHANGED
//            putExtra(WidgetConstants.ARGUMENT_IS_PLAYING, playbackStateCompat.isPlaying)
//        }
//        context.sendBroadcast(intent)
//    }
//
//    private fun sendWidgetMetadataIntent(metadata: MediaMetadataCompat) {
//        val intent = Intent(context, MusicAppWidget::class.java).apply {
//            action = WidgetConstants.METADATA_CHANGED
//            putExtra(WidgetConstants.ARGUMENT_SONG_ID, metadata.id)
//            putExtra(WidgetConstants.ARGUMENT_TITLE, metadata.title)
//            putExtra(WidgetConstants.ARGUMENT_SUBTITLE, metadata.displaySubtitle)
//            putExtra(WidgetConstants.ARGUMENT_COVER_URI, metadata.displayIconUri.toString())
//        }
//        context.sendBroadcast(intent)
//    }

    fun disconnect() = mediaBrowser.disconnect()

    fun changePlayMode(currentMode: SupportedPlayMode?) {
        when (currentMode) {
            SupportedPlayMode.REPEAT -> {
                transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)

            }

            SupportedPlayMode.REPEAT_ONE -> {
                transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
            }

            SupportedPlayMode.SHUFFLE -> {
                transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
            }
            else -> {

            }
        }
    }


    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: MusicServiceConnection? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: MusicServiceConnection(context)
                    .also { instance = it }
            }
    }

}

enum class SupportedPlayMode(value: Int) {
    REPEAT(PlaybackStateCompat.REPEAT_MODE_ALL),
    REPEAT_ONE(PlaybackStateCompat.REPEAT_MODE_ONE),
    SHUFFLE(PlaybackStateCompat.SHUFFLE_MODE_ALL)
}

@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()