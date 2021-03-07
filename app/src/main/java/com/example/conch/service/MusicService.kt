package com.example.conch.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.conch.data.TrackRepository
import com.example.conch.data.local.PersistentStorage
import com.example.conch.extension.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


class MusicService : MediaBrowserServiceCompat(), CoroutineScope by MainScope() {

    private val serviceScope = CoroutineScope(coroutineContext + SupervisorJob())

    private lateinit var session: MediaSessionCompat

    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private lateinit var player: SimpleExoPlayer

    private lateinit var notificationManager: MusicNotificationManager

    //播放列表
    private var playlist: List<MediaMetadataCompat> = emptyList()

    private lateinit var mediaSource: MediaSource

    private val remote = "http://conch-music.oss-cn-hangzhou.aliyuncs.com/track/"

    private var isForegroundService = false

    private val playerListener = PlayerEventListener()

    private lateinit var storage: PersistentStorage

    private var trackRepository = TrackRepository.getInstance()

    private val dataSourceFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, MUSIC_USER_AGENT), null)
    }

    override fun onCreate() {
        super.onCreate()

        //点击通知,打开App
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        session = MediaSessionCompat(this, TAG).apply {

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                            or PlaybackStateCompat.ACTION_PLAY_FROM_URI
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )

            setPlaybackState(stateBuilder.build())

            setSessionToken(sessionToken)
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }

        initExoPlayer()
        initMediaSessionConnector()

        launch {
            mediaSource = trackRepository.fetchTracksFromLocation(this@MusicService)
                .toMediaMetadataCompat()
                .toMediaSource(dataSourceFactory)
        }

        notificationManager = MusicNotificationManager(
            this,
            session.sessionToken,
            PlayerNotificationListener()
        )
        notificationManager.showNotificationForPlayer(player)

        storage = PersistentStorage.getInstance(applicationContext)

    }

    private fun initMediaSessionConnector() {
        mediaSessionConnector = MediaSessionConnector(session).apply {
            setPlayer(player)
            setQueueNavigator(object : TimelineQueueNavigator(session) {
                override fun getMediaDescription(
                    player: Player,
                    windowIndex: Int
                ): MediaDescriptionCompat =
                    playlist[windowIndex].description

            })
            setPlaybackPreparer(getPlaybackPreparer())
        }
    }


    private fun getPlaybackPreparer() = object : MediaSessionConnector.PlaybackPreparer {
        override fun onCommand(
            p0: Player,
            p1: ControlDispatcher,
            p2: String,
            p3: Bundle?,
            p4: ResultReceiver?
        ): Boolean = false

        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_URI or
                    PlaybackStateCompat.ACTION_PREPARE or
                    PlaybackStateCompat.ACTION_SEEK_TO


        override fun onPrepare(playWhenReady: Boolean) {

            val recentSong = storage.loadRecentSong() ?: return
            onPrepareFromMediaId(
                recentSong.mediaId!!,
                playWhenReady,
                recentSong.description.extras
            )
        }

        /**
         *  调用[transportControls.playFromMediaId()]时被触发
         */
        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            launch {
                //根据mediaID从本地找到对应的音频文件
                val itemToPlay: MediaMetadataCompat? =
                    trackRepository.fetchTracksFromLocation(this@MusicService).find { item ->
                        item.id.toString() == mediaId
                    }?.toMediaMetadataCompat()

                Log.d(TAG, "itemToPlay:${itemToPlay?.title.toString()}")

                //TODO 替换为当前播放列表
                val playlist =
                    trackRepository.getCachedLocalTracks(this@MusicService).toMediaMetadataCompat()

                val playbackStartPositionMs =
                    extras?.getLong(
                        MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS,
                        C.TIME_UNSET
                    )
                        ?: C.TIME_UNSET

                if (itemToPlay == null) {
                    Log.w(TAG, "Content not found: MediaID=$mediaId")
                } else {
                    preparePlaylist(
                        playlist,
                        itemToPlay,
                        playWhenReady,
                        playbackStartPositionMs
                    )
                }
            }
        }

        override fun onPrepareFromSearch(p0: String, p1: Boolean, p2: Bundle?) = Unit

        override fun onPrepareFromUri(uri: Uri, p1: Boolean, p2: Bundle?) = Unit

    }

    private fun initExoPlayer() {

        val musicAudioAttributes = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = SimpleExoPlayer.Builder(this).build().apply {
            setHandleAudioBecomingNoisy(true)
            audioAttributes = musicAudioAttributes
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
    }

    private fun preparePlaylist(
        metadataList: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playWhenReady: Boolean,
        playbackStartPositionMs: Long
    ) {
        val initialWindowIndex =
            if (itemToPlay == null) 0 else metadataList.indexOfFirst { metadata ->
                metadata.id == itemToPlay.id
            }

        playlist = metadataList

        player.playWhenReady = playWhenReady
        player.stop(true)
        val mediaSource = metadataList.toMediaSource(dataSourceFactory)
        player.setMediaSource(mediaSource)
        player.prepare()
        player.seekTo(initialWindowIndex, playbackStartPositionMs)
    }

    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.showNotificationForPlayer(player)
                    // If playback is paused we remove the foreground state which allows the
                    // notification to be dismissed. An alternative would be to provide a "close"
                    // button in the notification which stops playback and clears the notification.
                    if (playbackState == Player.STATE_READY) {
                        if (!playWhenReady) stopForeground(false)
                    }
                }
                else -> {
                    notificationManager.hideNotification()
                }
            }
        }


        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            mediaItem?.let {
                serviceScope.launch {
                    val trackId =
                        mediaItem.mediaId.substringAfter("content://media/external/audio/media/")
                            .toLong()
                    trackRepository.updateRecentPlay(trackId)
                }
            }

        }

        override fun onPlayerError(error: ExoPlaybackException) {
            var message = "出现未知错误"
            when (error.type) {
                // If the data from MediaSource object could not be loaded the Exoplayer raises
                // a type_source error.
                // An error message is printed to UI via Toast message to inform the user.
                ExoPlaybackException.TYPE_SOURCE -> {
                    message = "没有找到该媒体信息"
                    Log.e(TAG, "TYPE_SOURCE: " + error.sourceException.message)
                }
                // If the error occurs in a render component, Exoplayer raises a type_remote error.
                ExoPlaybackException.TYPE_RENDERER -> {
                    Log.e(TAG, "TYPE_RENDERER: " + error.rendererException.message)
                }
                // If occurs an unexpected RuntimeException Exoplayer raises a type_unexpected error.
                ExoPlaybackException.TYPE_UNEXPECTED -> {
                    Log.e(TAG, "TYPE_UNEXPECTED: " + error.unexpectedException.message)
                }
                // Occurs when there is a OutOfMemory error.
                ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                    Log.e(TAG, "TYPE_OUT_OF_MEMORY: " + error.outOfMemoryError.message)
                }
                // If the error occurs in a remote component, Exoplayer raises a type_remote error.
                ExoPlaybackException.TYPE_REMOTE -> {
                    Log.e(TAG, "TYPE_REMOTE: " + error.message)
                }
                ExoPlaybackException.TYPE_TIMEOUT -> {
                    Log.e(TAG, "TYPE_TIMEOUT: " + error.message)
                }
            }
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }


    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@MusicService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        saveRecentSongToStorage()
        super.onTaskRemoved(rootIntent)

        /**
         * By stopping playback, the player will transition to [Player.STATE_IDLE] triggering
         * [Player.EventListener.onPlayerStateChanged] to be called. This will cause the
         * notification to be hidden and trigger
         * [PlayerNotificationManager.NotificationListener.onNotificationCancelled] to be called.
         * The service will then remove itself as a foreground service, and will call
         * [stopSelf].
         */
        player.stop(/* reset= */true)
    }

    private fun saveRecentSongToStorage() {

        // Obtain the current song details *before* saving them on a separate thread, otherwise
        // the current player may have been unloaded by the time the save routine runs.
        val description = playlist[player.currentWindowIndex].description
        val position = player.currentPosition

        serviceScope.launch {
            storage.saveRecentSong(
                description,
                position
            )
        }
    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MY_EMPTY_MEDIA_ROOT_ID -> {
                result.sendResult(ArrayList())
            }
            MY_MEDIA_ROOT_ID -> {
                result.detach()//将信息从当前线程中移除，允许后续调用sendResult方法
                val platItems = ArrayList<MediaMetadataCompat>()
                val platitems = playlist.forEach {
                }
                val metadata = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, remote)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "原神")
                    .build()
                val mediaItems = mutableListOf(createMediaItem(metadata))
                result.sendResult(mediaItems)
            }
        }
    }

    private fun createMediaItem(metadata: MediaMetadataCompat): MediaBrowserCompat.MediaItem {
        return MediaBrowserCompat.MediaItem(
            metadata.description,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }
}

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

const val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"

private const val MUSIC_USER_AGENT = "music.agent"

private const val TAG = "MusicService"

