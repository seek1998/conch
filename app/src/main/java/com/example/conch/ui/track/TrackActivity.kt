package com.example.conch.ui.track

import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.databinding.ActivityTrackBinding
import com.example.conch.extension.getFormattedDuration
import com.example.conch.utils.InjectUtil

class TrackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackBinding

    private lateinit var viewModel: TrackViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)

        viewModel = InjectUtil.provideTrackViewModel(this)

        initDataBinding()
        setupNowPlaying()
        setUpButtons()
        setUpSeekBar()

        viewModel.mediaMetadata.observe(this) {
            updateUI(it)
        }

        viewModel.mediaButtonRes.observe(this) {
            binding.btnPlay.setImageState(it, true)
        }
    }


    private fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun setUpButtons() {
        binding.btnPlay.setOnClickListener {
            viewModel.playOrPause()
        }

        binding.btnNextTrack.setOnClickListener {
            viewModel.skipToNext()
        }

        binding.btnPreviousTrack.setOnClickListener {
            viewModel.skipToPrevious()
        }
    }

    private fun initDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_track)
        binding.lifecycleOwner = this
        viewModel.mediaMetadata.value = NowPlayingMetadata()
        binding.uiNowPlayerMetadata = viewModel.mediaMetadata.value
    }

    override fun onStart() {
        super.onStart()
        //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnect(this)
        //断开连接
    }

    private fun setupNowPlaying() {
        binding.seekbar.max = viewModel.mediaMetadata.value!!._duration
    }

    private fun updateUI(metadata: NowPlayingMetadata) = with(binding) {
        uiNowPlayerMetadata = metadata
        uiNowPlayerMetadata
        //更新图片
        if (metadata.albumArtUri.toString() == "") return@with

        setupTopArt(metadata.albumArtUri)
    }

    //加载专辑图片
    private fun setupTopArt(uri: Uri) {

        Glide.with(this)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivAlbumImage)
    }


    private fun setUpSeekBar() {
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val formattedProgress = progress.getFormattedDuration()
                binding.trackProgressMax.text = formattedProgress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //通知service调整音轨进度
                //viewModel.changeTrackProgress(seekBar!!.progress)
            }

        })
    }


}