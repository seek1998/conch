package com.example.conch.ui.main.account

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.data.model.User
import com.example.conch.databinding.FragmentAccountBinding
import com.example.conch.service.MessageEvent
import com.example.conch.service.MessageType
import com.example.conch.ui.BaseFragment
import com.example.conch.ui.adapter.PlaylistAdapter
import com.example.conch.ui.adapter.RecentPlayAdapter
import com.example.conch.ui.login.LoginActivity
import com.example.conch.ui.main.MainViewModel
import com.example.conch.ui.playlist.PlaylistActivity
import com.example.conch.ui.track.TrackActivity
import com.example.conch.ui.user.UserActivity
import com.example.conch.utils.InjectUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class AccountFragment : BaseFragment<FragmentAccountBinding, AccountViewModel>() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var tvPlaylistTitle: TextView

    private lateinit var tvPlaylistDescription: TextView

    private val mainViewModel by activityViewModels<MainViewModel> {
        InjectUtil.provideMainViewModelFactory(requireActivity())
    }

    private lateinit var eventBus: EventBus

    override fun processLogic() {

        eventBus = EventBus.getDefault()

        viewModel.loadAllPlaylist()

        setUpUserInfo()

        setUpPlaylistRecycleView()

        setUpRecentPlayRecycleView()

        viewModel.favorites.observe(this@AccountFragment, {
            it?.let {
                setUpFavorite(it)
            }
        })
    }

    private fun setUpUserInfo(user: User = mainViewModel.getUser()) = with(binding) {

        accountToLoginOrUserInfo.apply {

            if (mainViewModel.isLoggedIn()) {
                //已经登录
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_forward
                    )
                )

                setOnClickListener {

                    val intent = Intent(requireActivity(), UserActivity::class.java).apply {
                        putExtra("user", user)
                    }

                    startActivity(intent)
                }

            } else {
                //未登录
                setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_login))

                setOnClickListener {

                    val intent = Intent(requireActivity(), LoginActivity::class.java)

                    startActivity(intent)
                }
            }
        }

        if (user.id == User.LOCAL_USER) {
            accountUsername.text = "本地用户"
        } else {
            accountUsername.text = user.name
        }

        accountEmail.text = user.email

    }


    @SuppressLint("SetTextI18n")
    private fun setUpFavorite(favorites: Playlist) = with(binding) {

        btnToFavorite.setOnClickListener {

            val intent = Intent(this@AccountFragment.activity, PlaylistActivity::class.java).apply {
                putExtra("playlist", favorites)
            }

            startActivity(intent)
        }

        ivPlaylistFavoriteCover.apply {

            scope.launch {
                val coverPath = mainViewModel.getPlaylistCover(favorites).await()

                requireActivity().runOnUiThread {
                    Glide.with(this@apply)
                        .load(coverPath)
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(this@apply)
                }
            }
        }

        tvPlaylistFavoriteCount.text = "${favorites.size}" + "首"
    }

    private fun setUpRecentPlayRecycleView() {

        val recentPlayLayoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        val recentPlayAdapter = RecentPlayAdapter(this::recentPlayItemOnClick)

        binding.rvRecentPlay.apply {
            layoutManager = recentPlayLayoutManager
            adapter = recentPlayAdapter
            isSaveEnabled = true
            isSaveFromParentEnabled = true
        }

        mainViewModel.recentPlay.observe(this, {
            it?.let {

                if (it.isEmpty()) {
                    binding.tvRecentPlay.text = getString(R.string.no_recent_play)
                    return@let
                }

                recentPlayAdapter.submitList(it as MutableList<Track>)

                handler.postDelayed({
                    binding.rvRecentPlay.scrollToPosition(0)
                }, 100)

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

                val newList =
                    it.dropWhile { playlist -> playlist.id == Playlist.PLAYLIST_FAVORITE_ID }

                playlistAdapter.submitList(newList as MutableList<Playlist>)

                viewModel.updateFavorites()
            }
        })
    }

    private fun createNewPlaylist() {

        val contentView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_playlist, null)

        this.tvPlaylistTitle = contentView.findViewById(R.id.fg_tv_playlist_title)
        this.tvPlaylistDescription = contentView.findViewById(R.id.fg_tv_playlist_description)

        MaterialAlertDialogBuilder(requireContext())
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
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                run {
                    dialog.cancel()
                }
            }
            .show()
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

    override fun onStart() {
        super.onStart()
        eventBus.register(this)
    }

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(message: MessageEvent) {
        if (message.type == MessageType.ACTION_UPDATE_USER_INFO) {
            val user = message.getParcelable<User>()!!
            this.setUpUserInfo(user)
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_account

    override fun getViewModelInstance() =
        AccountViewModel(requireActivity().application)

    override fun getViewModelClass(): Class<AccountViewModel> = AccountViewModel::class.java

}

private const val TAG = "AccountFragment"