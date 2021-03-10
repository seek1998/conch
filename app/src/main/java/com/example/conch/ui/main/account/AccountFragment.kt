package com.example.conch.ui.main.account

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conch.R
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.databinding.FragmentAccountBinding
import com.example.conch.ui.BaseFragment
import com.example.conch.ui.login.LoginActivity
import com.example.conch.ui.main.MainViewModel
import com.example.conch.ui.plalylist.PlaylistActivity
import com.example.conch.ui.track.TrackActivity
import com.example.conch.utils.InjectUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class AccountFragment : BaseFragment<FragmentAccountBinding, AccountViewModel>() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var tvPlaylistTitle: TextView

    private lateinit var tvPlaylistDescription: TextView

    private val mainViewModel by activityViewModels<MainViewModel> {
        InjectUtil.provideMainViewModelFactory(requireActivity())
    }

    override fun processLogic() {

        viewModel.loadAllPlaylist()

        binding.btnToFavorite.setOnClickListener {

            val favoritePlaylist = viewModel.getFavoritePlaylist()

            val intent = Intent(this.activity, PlaylistActivity::class.java).apply {
                putExtra("playlist", favoritePlaylist)
            }
            startActivity(intent)
        }

        binding.btnAccountLogin.setOnClickListener {
            startActivity(Intent(this.activity, LoginActivity::class.java))
        }


        setUpPlaylistRecycleView()
        setUpRecentPlayRecycleView()
        binding.rvRecentPlay.isFocusable = true
    }

    private fun setUpRecentPlayRecycleView() {

        val recentPlayLayoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        val recentPlayAdapter = RecentPlayAdapter(this::recentPlayItemOnClick, requireContext())

        binding.rvRecentPlay.apply {
            layoutManager = recentPlayLayoutManager
            adapter = recentPlayAdapter
            isSaveEnabled = true
            isSaveFromParentEnabled = true
        }

        mainViewModel.recentPlay.observe(this, {
            it?.let {
                Log.d(TAG, it.toString())
                if (it.isEmpty()) {
                    binding.tvRecentPlay.text = getString(R.string.no_recent_play)
                    return@let
                }
                recentPlayAdapter.submitList(it as MutableList<Track>)

                handler.postDelayed({
                    binding.rvRecentPlay.scrollToPosition(0)
                }, 500)

            }
        })
    }

    private fun setUpPlaylistRecycleView() {

        val playlistLayoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        val playlistAdapter = PlaylistAdapter(this::playlistItemOnClick, this::createNewPlaylist)

        binding.rvPlaylist.apply {
            layoutManager = playlistLayoutManager
            adapter = playlistAdapter
            isSaveEnabled = true
            isSaveFromParentEnabled = true
        }

        viewModel.playlists.observe(this, {

            it?.let {

                if (it.isEmpty()) return@observe

                val newList =
                    it.dropWhile { playlist -> playlist.id == Playlist.PLAYLIST_FAVORITE_ID }

                playlistAdapter.submitList(newList as MutableList<Playlist>)
            }
        })
    }

    private fun createNewPlaylist() {

        val contentView = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_dialog_create_playlist, null)

        this.tvPlaylistTitle = contentView.findViewById(R.id.fg_tv_playlist_title)
        this.tvPlaylistDescription = contentView.findViewById(R.id.fg_tv_playlist_description)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("新建歌单")
            .setView(contentView)
            .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                run {


                    val title = tvPlaylistTitle.text.trim().toString()
                    val description = tvPlaylistDescription.text.trim().toString()

                    if (title.isEmpty()) {
                        toast(getString(R.string.empty_playlist_title_error))
                        return@run
                    }

                    viewModel.createNewPlaylist(title, description)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                {
                    run {
                        dialog.cancel()
                    }
                }
            }
            .show()
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun playlistItemOnClick(playlist: Playlist) {
        val intent = Intent(requireActivity(), PlaylistActivity::class.java).apply {
            putExtra("playlist", playlist)
        }
        startActivity(intent)
    }

    private fun recentPlayItemOnClick(track: Track) {
        mainViewModel.playTrack(track)
        val intent = Intent(requireActivity(), TrackActivity::class.java)
        startActivity(intent)
    }

    override fun getLayoutId(): Int = R.layout.fragment_account

    override fun getViewModelInstance() =
        AccountViewModel(requireActivity().application)

    override fun getViewModelClass(): Class<AccountViewModel> = AccountViewModel::class.java

}

private const val TAG = "AccountFragment"