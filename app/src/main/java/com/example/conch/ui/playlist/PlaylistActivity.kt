package com.example.conch.ui.playlist

import android.content.Intent
import android.graphics.Color
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.databinding.ActivityPlaylistBinding
import com.example.conch.service.MessageEvent
import com.example.conch.service.MessageType
import com.example.conch.ui.BaseActivity
import com.example.conch.ui.adapter.LocalTrackAdapter
import com.example.conch.ui.track.TrackActivity
import com.example.conch.utils.InjectUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jaeger.library.StatusBarUtil
import org.greenrobot.eventbus.EventBus

class PlaylistActivity : BaseActivity<ActivityPlaylistBinding, PlaylistViewModel>() {

    private lateinit var trackAdapter: LocalTrackAdapter

    override fun processLogic() {

        changeStatusBar()

        val playlist = intent.getParcelableExtra<Playlist>("playlist")

        viewModel.playlistLiveData.postValue(playlist)

        trackAdapter = LocalTrackAdapter({ track: Track -> itemOnClick(track) },
            { track: Track -> trackOptionsOnClick(track) })

        binding.rv.apply {
            adapter = trackAdapter
            isSaveEnabled = true
            scheduleLayoutAnimation()
        }

        binding.toolBar.setNavigationOnClickListener {
            finish()
        }

        viewModel.playlistLiveData.observe(this, {

            it?.let {
                binding.toolBarLayout.title = it.title
                binding.acPlaylistTvDescription.text = it.description
                viewModel.getPlaylistTracks(it.id)
                viewModel.getPlaylistCover(it.id)
                setupToolbar(it.id)
            }

        })

        viewModel.tracksLiveData.observe(this, {

            it?.let {

                if (it.isEmpty()) {
                    binding.toolBarLayout.subtitle = getString(R.string.no_track)
                    return@let
                }

                trackAdapter.submitList(it as MutableList<Track>)
                binding.toolBarLayout.subtitle = "共有${it.size}首歌曲"
                binding.acPlaylistTvBottom.visibility = View.VISIBLE
            }
        })

        viewModel.coverPathLiveData.observe(this, {
            it?.let {

                if (it.isEmpty() || it.isBlank()) {
                    return@let
                }

                Glide.with(this)
                    .load(it)
                    .override(1024, 1024)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(binding.acPlaylistCover)
            }
        })
    }

    private fun trackOptionsOnClick(track: Track) {

    }

    private fun setupToolbar(playlistId: Long) {

        if (playlistId == Playlist.PLAYLIST_FAVORITE_ID) {
            binding.toolBar.menu.setGroupVisible(0, false)
            return
        }

        binding.toolBar.apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete -> {
                        showDialogIfDeletePlaylist()
                        true
                    }
                    else -> true
                }
            }


        }
    }

    private fun showDialogIfDeletePlaylist() {
        MaterialAlertDialogBuilder(this)
            .setTitle("删除歌单")
            .setPositiveButton(getText(R.string.yes)) { dialog, which ->
                run {
                    viewModel.deletePlaylist()
                    this.finish()
                    EventBus.getDefault()
                        .postSticky(MessageEvent(MessageType.ACTION).put("refresh_playlists"))
                }
            }
            .setNegativeButton(getText(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun changeStatusBar() {
        StatusBarUtil.setDarkMode(this)
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun itemOnClick(track: Track) {
        viewModel.playTrack(track)
        startActivity(Intent(this, TrackActivity::class.java))
        finish()
    }

    override fun getLayoutId() = R.layout.activity_playlist

    override fun getViewModelInstance() =
        InjectUtil.providePlaylistViewModelFactory(this).create(PlaylistViewModel::class.java)

    override fun getViewModelClass() = PlaylistViewModel::class.java
}

private const val TAG = "PlaylistActivity"