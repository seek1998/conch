package com.example.conch.ui.track

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.databinding.ActivityTrackBinding
import com.example.conch.extension.getFormattedDuration
import com.example.conch.service.SupportedPlayMode
import com.example.conch.utils.InjectUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.floor

class TrackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackBinding

    private lateinit var viewModel: TrackViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)

        viewModel = InjectUtil.provideTrackViewModel(this)

        initDataBinding()
        setUpButtons()
        setUpSeekBar()

        viewModel.nowPlayingMetadata.observe(this) {
            updateUI(it)
        }

        viewModel.mediaPosition.observe(this) {
            binding.seekbar.progress = floor(it / 1E3).toInt()
        }

        viewModel.mediaButtonRes.observe(this) {
            binding.btnPlay.setImageState(it, true)
        }

        viewModel.playMode.observe(this) {

            binding.btnPlayMode.setImageLevel(
                when (it) {
                    SupportedPlayMode.REPEAT -> {
                        toast(this.getString(R.string.mode_repeat))
                        0
                    }
                    SupportedPlayMode.REPEAT_ONE -> {
                        toast(this.getString(R.string.mode_repeat_one))
                        1
                    }
                    SupportedPlayMode.SHUFFLE -> {
                        toast(this.getString(R.string.mode_shuffle))
                        2
                    }
                    else -> 0
                }
            )

        }
    }


    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setUpButtons() {

        binding.toolBar.apply {
            setNavigationOnClickListener {
                this@TrackActivity.finish()
            }

        }

        binding.btnPlay.setOnClickListener {
            viewModel.playOrPause()
        }

        binding.btnNextTrack.setOnClickListener {
            viewModel.skipToNext()
        }

        binding.btnPreviousTrack.setOnClickListener {
            viewModel.skipToPrevious()
        }

        binding.btnPlayMode.setOnClickListener {
            viewModel.changePlayMode()
        }

        binding.btnQueueTrack.setOnClickListener {
            val items = viewModel.getQueueTrack().toTypedArray()

            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.queue_track))
                .setItems(items, DialogInterface.OnClickListener { dialog, which ->

                })
                .show()
        }
    }

    private fun initDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_track)
        binding.lifecycleOwner = this
        binding.uiNowPlayingMetadata = NowPlayingMetadata()
        //使标题textView开启跑马灯
        binding.tvTrackTitle.apply {
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.MARQUEE
            isSelected = true
            isFocusableInTouchMode = true
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun updateUI(metadata: NowPlayingMetadata) = with(binding) {

        uiNowPlayingMetadata = metadata
        seekbar.max = metadata._duration
        trackProgressMax.text = metadata.duration
        //更新图片
        setupAlbumArt(metadata.albumArtUri)
    }

    //加载专辑图片
    private fun setupAlbumArt(uri: Uri) {

        if (uri.toString() == "") {
            Glide.with(this)
                .load(R.drawable.ic_music_note_big)
                .into(binding.ivAlbumImage)

            return
        }

        Glide.with(this)
            .load(uri)
            .placeholder(binding.ivAlbumImage.drawable)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivAlbumImage)

    }

    private fun setUpSeekBar() {
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.trackProgressCurrent.text = progress.getFormattedDuration()
                Log.d(TAG, "新位置$progress")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.changeTrackProgress(seekBar!!.progress)
            }

        })
    }
}

private const val TAG = "TrackActivity"
