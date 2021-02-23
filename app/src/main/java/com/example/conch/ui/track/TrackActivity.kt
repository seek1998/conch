package com.example.conch.ui.track

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
import com.example.conch.service.MessageEvent
import com.example.conch.service.MessageType
import com.example.conch.service.SupportedPlayMode
import com.example.conch.utils.InjectUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TrackActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName

    private lateinit var binding: ActivityTrackBinding

    private lateinit var viewModel: TrackViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)

        viewModel = InjectUtil.provideTrackViewModel(this)

        initDataBinding()
        setUpButtons()
        setUpSeekBar()

        viewModel.mediaMetadata.observe(this) {
            updateUI(it)
        }

        viewModel.mediaButtonRes.observe(this) {
            binding.btnPlay.setImageState(it, true)
        }

        viewModel._playMode.observe(this) {

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

    //接收消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.currduration -> {
                binding.seekbar.progress = event.getInt()

            }
            else -> {

            }
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
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnect(this)
        EventBus.getDefault().unregister(this)
    }

    private fun updateUI(metadata: NowPlayingMetadata) = with(binding) {

        uiNowPlayingMetadata = metadata
        Log.d(TAG, uiNowPlayingMetadata.toString())
        binding.seekbar.progress = 0
        binding.seekbar.max = viewModel.mediaMetadata.value!!._duration
        //更新图片
        setupTopArt(metadata.albumArtUri)
    }

    //加载专辑图片
    private fun setupTopArt(uri: Uri) {

        Log.d(TAG, uri.toString())
        if (uri.toString() == "") {
            Glide.with(this)
                .load(R.drawable.ic_music_note_big)
                .into(binding.ivAlbumImage)
        } else {
            Glide.with(this)
                .load(uri)
                .placeholder(binding.ivAlbumImage.drawable)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivAlbumImage)
        }


    }


    private fun setUpSeekBar() {
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val formattedProgress = progress.getFormattedDuration()
                binding.trackProgressCurrent.text = formattedProgress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d("seekbar", "开始拖动")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.changeTrackProgress(seekBar!!.progress)
                Log.d("seekbar", "拖动结束， 新位置${seekBar.progress}")
            }

        })
    }


}