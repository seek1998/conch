package com.example.conch.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.conch.service.MessageEvent
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

    private val TAG = this::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {

        EventBus.getDefault().register(this)

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
                putExtra("now_playing", viewModel.nowPlayingMetadata.value)
            }
            startActivity(intent)
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(message: MessageEvent) {
        when (message.getString()) {
            "refresh_playlists" -> viewModel.loadAllPlaylist()
        }
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