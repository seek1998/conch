package com.example.conch.ui.plalylist

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.databinding.ActivityPlaylistBinding
import com.example.conch.ui.BaseActivity
import com.example.conch.ui.main.local.LocalTrackAdapter
import com.example.conch.ui.track.TrackActivity
import com.example.conch.utils.InjectUtil

class PlaylistActivity : BaseActivity<ActivityPlaylistBinding, PlaylistViewModel>() {

    private lateinit var trackAdapter: LocalTrackAdapter

    override fun processLogic() {

        changeStatusBar()

        viewModel.playlistLiveData.postValue(intent.getParcelableExtra("playlist") ?: Playlist())

        trackAdapter = LocalTrackAdapter { track -> itemOnClick(track) }

        binding.rv.apply {
            adapter = trackAdapter
            isSaveEnabled = true
        }

        binding.toolBar.setNavigationOnClickListener {
            finish()
        }

        viewModel.playlistLiveData.observe(this, {

            it?.let {
                binding.toolBarLayout.title = it.title
                viewModel.getPlaylistTracks(it.id)
                viewModel.getPlaylistCover(it.id)
            }

        })

        viewModel.tracksLiveData.observe(this, {

            it?.let {

                if (it.isEmpty()) {
                    toast(getString(R.string.playlist_has_nothing))
                    binding.toolBarLayout.subtitle = getString(R.string.no_track)
                    return@let
                }

                trackAdapter.submitList(it as MutableList<Track>)
                binding.toolBarLayout.subtitle = "共有${it.size}首歌曲"
                binding.acPlaylistRvBottom.visibility = View.VISIBLE
            }
        })

        viewModel.coverPathLiveData.observe(this, {
            it?.let {
                if (it.isEmpty() || it.isBlank()) {
                    return@let
                }

                Glide.with(this)
                    .load(it)
                    .override(512, 512)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(binding.acPlaylistCover)
            }
        })
    }

    private fun changeStatusBar() {
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun itemOnClick(track: Track) {
        viewModel.playTrack(track)
        startActivity(Intent(this, TrackActivity::class.java))
        finish()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun getLayoutId() = R.layout.activity_playlist

    override fun getViewModelInstance() =
        InjectUtil.providePlaylistViewModelFactory(this).create(PlaylistViewModel::class.java)

    override fun getViewModelClass() = PlaylistViewModel::class.java
}

private const val TAG = "PlaylistActivity"