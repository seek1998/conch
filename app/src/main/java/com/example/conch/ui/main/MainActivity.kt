package com.example.conch.ui.main

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.data.MyResult
import com.example.conch.data.model.Track
import com.example.conch.service.MessageEvent
import com.example.conch.service.MessageType
import com.example.conch.ui.track.TrackActivity
import com.example.conch.utils.InjectUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var mainPlayCard: CardView

    private lateinit var tvNowPlayingTitle: MaterialTextView

    private lateinit var tvNowPlayingArtist: MaterialTextView

    private lateinit var ivNowPlayingCover: ShapeableImageView

    private lateinit var btnPlayPause: ShapeableImageView

    private val viewModel by viewModels<MainViewModel> {
        InjectUtil.provideMainViewModelFactory(this)
    }

    private val remoteTrackViewModel by viewModels<RemoteTrackIOViewModel> {
        InjectUtil.provideRemoteTrackViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainPlayCard = findViewById(R.id.main_card)
        tvNowPlayingTitle = findViewById(R.id.main_tv_title)
        tvNowPlayingArtist = findViewById(R.id.main_tv_artist)
        ivNowPlayingCover = findViewById(R.id.main_iv_cover)
        btnPlayPause = findViewById(R.id.main_btn_playpause)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        navView.setupWithNavController(navController)

        viewModel.checkRecentPlay()

        viewModel.loadAllPlaylist()

        viewModel.nowPlayingMetadata.observe(this, {
            it?.let {
                Log.i(TAG, it.toString())

                it.title?.let { str ->
                    if (str.isNotEmpty()) {
                        tvNowPlayingTitle.text = str
                    }
                }

                it.subtitle?.let { str ->
                    if (str.isNotEmpty()) {
                        tvNowPlayingArtist.text = str
                    }
                }

                setupAlbumArt(it.albumArtUri)
            }
        })

        viewModel.btnPlayPauseLevel.observe(this, {
            it?.let {
                btnPlayPause.setImageLevel(it)
            }
        })

        btnPlayPause.setOnClickListener {
            viewModel.playOrPause()
        }

        mainPlayCard.setOnClickListener {
            val intent = Intent(this, TrackActivity::class.java).apply {
                val nowPlaying = viewModel.nowPlayingMetadata.value
                putExtra("now_playing", nowPlaying)
            }
            startActivity(intent)
        }

        observerRemoteIOProgress()

        viewModel.postTrackInfoResult.observe(this, {
            it?.let {
                if (it is MyResult.Success) {
                    it.data?.let { track ->
                        remoteTrackViewModel.addTrackToUploadQueue(track)
                    }

                } else if (it is MyResult.Error) {
                    it.exception.message?.let { message ->
                        toast(message)
                    }
                }
            }
        })


    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    fun onEvent(message: MessageEvent) {
        when (message.getString()) {
            "refresh_playlists" -> viewModel.loadAllPlaylist()
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onPlaylistEvent(message: MessageEvent) {
        if (message.type == MessageType.PLAYLIST_ACTION_ADD_TRACK) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayEvent(message: MessageEvent) {
        if (message.type == MessageType.TRACK_DATA) {
            Log.d(TAG, "get event")
            message.getParcelable<Track>("track_to_play")?.let {
                Log.d(TAG, it.toString())
                viewModel.playTrack(track = it)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMediaScanEvent(messageEvent: MessageEvent) {
        if (messageEvent.type == MessageType.ACTION_UPDATE_MEDIA_STORE) {

            val path = application.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.path
            val types = arrayOf("audio/mpeg", "audio/flac", "audio/x-wav, image/jpeg")
            Log.d(TAG, "Path: $path")
            MediaScannerConnection.scanFile(
                applicationContext,
                arrayOf(path),
                types,
                object : MediaScannerConnection.MediaScannerConnectionClient {
                    override fun onScanCompleted(path: String?, uri: Uri?) {

                        //扫描完成后，更新应用数据库
                        viewModel.refreshLocalData(applicationContext)
                        Log.d(TAG, "Uri: ${uri.toString()}")
                    }

                    override fun onMediaScannerConnected() {

                    }
                })
        }

    }

    private fun observerRemoteIOProgress() {
        remoteTrackViewModel.currentUploadProgress.observe(
            this,
            remoteTrackViewModel.uploadProgressObserver
        )

        remoteTrackViewModel.succeedTrackUploadResult.observe(this, {
            it?.let {
                if (it is MyResult.Success) {
                    val succeedTrack = it.data
                    toast("${succeedTrack?.title}上传成功")
                }
            }
        })

        remoteTrackViewModel.errorResult.observe(this, {
            it?.let {
                if (it is MyResult.Error) {
                    val message = it.exception.message
                    toast("错误: $message")
                }
            }
        })

        remoteTrackViewModel.currentUploadTrack.observe(this, {
            it?.let {
                toast("开始上传：${it.title}")
            }
        })

        remoteTrackViewModel.currentDownloadTrack.observe(this, {
            it?.let {
                toast("开始下载：${it.title}")
            }
        })
    }

    private fun setupAlbumArt(uri: Uri) {

        if (uri == Uri.EMPTY) {
            Glide.with(this)
                .load(ContextCompat.getDrawable(this, R.drawable.ic_round_play_arrow_16_write))
                .into(ivNowPlayingCover)
            return
        }

        Glide.with(this)
            .load(uri)
            .placeholder(ivNowPlayingCover.drawable)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(ivNowPlayingCover)
    }

    private fun toast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).apply {
            setText(msg)
            show()
        }
    }


    private var pressedTime = 0L

    override fun onBackPressed() {

        val nowTime = System.currentTimeMillis()
        if (nowTime - pressedTime > 2000) {
            Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show()
            pressedTime = nowTime
        } else {
            this.finish()
            exitProcess(0)
        }
    }

}

private const val TAG = "MainActivity"
