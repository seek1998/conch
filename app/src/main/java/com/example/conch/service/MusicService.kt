package com.example.conch.service

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
import androidx.media.MediaBrowserServiceCompat
import com.example.conch.data.TrackRepository
import com.example.conch.extension.id
import com.example.conch.extension.toMediaMetadataCompat
import com.example.conch.extension.toMediaSource
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class MusicService : MediaBrowserServiceCompat(), CoroutineScope by MainScope() {

    private lateinit var session: MediaSessionCompat

    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private lateinit var exoPlayer: SimpleExoPlayer

    private val trackRepository = TrackRepository

    //当前播放列表
    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()

    private val remote = "http://conch-music.oss-cn-hangzhou.aliyuncs.com/track/"

    private val TAG = this.javaClass.simpleName

    override fun onCreate() {
        super.onCreate()

        session = MediaSessionCompat(baseContext, "conch").apply {

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

            isActive = true
        }

        initExoPlayer()
        initMediaSessionConnector()

    }

    private fun initMediaSessionConnector() {
        mediaSessionConnector = MediaSessionConnector(session).apply {
            setPlayer(exoPlayer)
            setQueueNavigator(object : TimelineQueueNavigator(session) {
                override fun getMediaDescription(
                    player: Player,
                    windowIndex: Int
                ): MediaDescriptionCompat =
                    currentPlaylistItems[windowIndex].description

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
                    PlaybackStateCompat.ACTION_PREPARE

        override fun onPrepare(playWhenReady: Boolean) {

            launch {

//                val playlist = TrackRepository.fetchTrackFromLocation(this@MusicService)
//                    .toMediaMetadataCompat()

                val playlist = TrackRepository.fetchTrackFromRemote(0)
                    .toMediaMetadataCompat()

                val itemToPlay = playlist.get(index = 0)

                preparePlaylist(
                    playlist,
                    itemToPlay,
                    playWhenReady,
                    0
                )
            }
        }

        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            launch {
                val itemToPlay =
                    TrackRepository.fetchTrackFromLocation(this@MusicService).find { item ->
                        item.id.toString() == mediaId
                    }?.toMediaMetadataCompat()

                val playlist = TrackRepository.fetchTrackFromLocation(this@MusicService)
                    .toMediaMetadataCompat()

                if (itemToPlay == null) {
                    Log.w(TAG, "Content not found: MediaID=$mediaId")
                } else {
                    preparePlaylist(
                        playlist,
                        itemToPlay,
                        playWhenReady,
                        0
                    )
                }
            }
        }

        override fun onPrepareFromSearch(p0: String, p1: Boolean, p2: Bundle?) = Unit

        override fun onPrepareFromUri(uri: Uri, p1: Boolean, p2: Bundle?) = Unit

    }

    private val dataSourceFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, MUSIC_USER_AGENT), null)
    }

    private fun initExoPlayer() {

        val musicAudioAttributes = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        exoPlayer = SimpleExoPlayer.Builder(this).build().apply {
            setHandleAudioBecomingNoisy(true)
            audioAttributes = musicAudioAttributes
            Toast.makeText(this@MusicService, "准备完成", Toast.LENGTH_SHORT).show()
        }
    }

    private fun preparePlaylist(
        metadataList: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?, playWhenReady: Boolean,
        playbackStartPositionMs: Long
    ) {
        val initialWindowIndex =
            if (itemToPlay == null) 0 else metadataList.indexOfFirst { metadata ->
                metadata.id == itemToPlay.id
            }
        currentPlaylistItems = metadataList

        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.stop(true)
        val mediaSource = metadataList.toMediaSource(dataSourceFactory)
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.seekTo(initialWindowIndex, playbackStartPositionMs)

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

private const val MUSIC_USER_AGENT = "music.agent"
