package com.example.conch.ui.track

import android.content.DialogInterface
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.databinding.ActivityTrackBinding
import com.example.conch.extension.getFormattedDuration
import com.example.conch.service.SupportedPlayMode
import com.example.conch.ui.BaseActivity
import com.example.conch.utils.InjectUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.floor

class TrackActivity : BaseActivity<ActivityTrackBinding, TrackViewModel>() {

    override fun processLogic() {

        initDataBinding()
        setUpButtons()
        setUpSeekBar()
        setUpToolBar()

        intent.getParcelableExtra<NowPlayingMetadata>("now_playing")?.let {
            viewModel.nowPlayingMetadata.postValue(it)
        }

        viewModel.nowPlayingMetadata.observe(this) {
            it?.let {
                updateUI(it)
            }
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

        viewModel.isFavorite.observe(this, {

            binding.btnFavorite.setImageLevel(
                if (it) {
                    1
                } else {
                    0
                }
            )

        })
    }

    private fun setUpToolBar() {

        binding.toolBar.apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.more -> {
                        true
                    }
                    else -> false
                }
            }

            setNavigationOnClickListener {
                this@TrackActivity.finish()
            }
        }
    }

    private fun setUpButtons() = with(binding) {

        btnPlay.setOnClickListener {
            viewModel.playOrPause()
        }

        btnNextTrack.setOnClickListener {
            viewModel.skipToNext()
        }

        btnPreviousTrack.setOnClickListener {
            viewModel.skipToPrevious()
        }

        btnPlayMode.setOnClickListener {
            viewModel.changePlayMode()
        }

        btnQueueTrack.setOnClickListener {
            val items = viewModel.getQueueTrack().toTypedArray()

            MaterialAlertDialogBuilder(this@TrackActivity)
                .setTitle(getString(R.string.queue_track))
                .setItems(items, DialogInterface.OnClickListener { dialog, which ->

                })
                .show()
        }

        btnFavorite.setOnClickListener {
            val isFavorite = viewModel.isFavorite.value!!

            if (isFavorite) {
                toast("取消收藏")
            } else {
                toast("加入收藏")
            }

            viewModel.changeFavoriteMode()
        }

        btnLibraryAdd.setOnClickListener {
            toast("添加到歌单")
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

    private fun updateUI(metadata: NowPlayingMetadata) = with(binding) {

        uiNowPlayingMetadata = metadata
        seekbar.max = metadata._duration
        trackProgressMax.text = metadata.duration
        //更新图片
        setupAlbumArt(metadata.albumArtUri, binding.ivAlbumImage)
        setupLastAndNextTrackCover()
    }

    private fun setupLastAndNextTrackCover() {

        viewModel.previousTrack?.let {
            setupAlbumArt(Uri.parse(it.coverPath), binding.ivPreviousAlbumImage)
        }

        viewModel.nextTrack?.let {
            setupAlbumArt(Uri.parse(it.coverPath), binding.ivNextAlbumImage)
        }
    }

    //加载专辑图片
    private fun setupAlbumArt(uri: Uri, target: ImageView) {

        if (uri.toString() == "" || uri == Uri.EMPTY) {
            Glide.with(this)
                .load(R.drawable.ic_music_note_big)
                .into(target)

            return
        }

        Glide.with(this)
            .load(uri)
            .placeholder(target.drawable)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(target)

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

    override fun getLayoutId(): Int = R.layout.activity_track

    override fun getViewModelInstance(): TrackViewModel =
        InjectUtil.provideTrackViewModel(this)

    override fun getViewModelClass(): Class<TrackViewModel> = TrackViewModel::class.java
}

private const val TAG = "TrackActivity"
