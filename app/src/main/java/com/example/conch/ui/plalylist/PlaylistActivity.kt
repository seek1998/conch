package com.example.conch.ui.plalylist

import android.content.Intent
import android.util.Log
import android.widget.Toast
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

        viewModel.test()

        viewModel.playlistLiveData.postValue(intent.getParcelableExtra("playlist") ?: Playlist())

        trackAdapter = LocalTrackAdapter { track -> itemOnClick(track) }

        binding.rv.apply {
            adapter = trackAdapter
            isSaveEnabled = true
        }

        viewModel.playlistLiveData.observe(this, {

            it?.let {
                binding.toolBarLayout.title = it.title
                viewModel.getPlaylistTracks(it.id)
            }

        })

        viewModel.tracksLiveData.observe(this, {

            Log.d(TAG, it.toString())
            it?.let {

                if (it.isEmpty()) {
                    ToastMessage(getString(R.string.playlist_has_nothing))
                    return@let
                }

                trackAdapter.submitList(it as MutableList<Track>)
                binding.toolBarLayout.subtitle = "共有${it.size}首歌曲"
            }
        })
    }

    private fun itemOnClick(track: Track) {
        viewModel.playTrack(track)
        startActivity(Intent(this, TrackActivity::class.java))
        finish()
    }

    private fun ToastMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun getLayoutId() = R.layout.activity_playlist

    override fun getViewModelInstance() =
        InjectUtil.providePlaylistViewModelFactory(this).create(PlaylistViewModel::class.java)

    override fun getViewModelClass() = PlaylistViewModel::class.java
}

private const val TAG = "PlaylistActivity"