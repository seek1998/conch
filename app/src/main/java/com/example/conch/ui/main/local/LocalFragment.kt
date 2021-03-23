package com.example.conch.ui.main.local

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.conch.R
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.databinding.FragmentLocalBinding
import com.example.conch.ui.BaseFragment
import com.example.conch.ui.adapter.LocalTrackAdapter
import com.example.conch.ui.dialog.PlaylistDialog
import com.example.conch.ui.main.MainViewModel
import com.example.conch.utils.InjectUtil
import com.example.conch.utils.SizeUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView


class LocalFragment : BaseFragment<FragmentLocalBinding, LocalViewModel>() {

    private lateinit var localTrackAdapter: LocalTrackAdapter

    private lateinit var playlistDialog: PlaylistDialog

    private val mainViewModel by activityViewModels<MainViewModel> {
        InjectUtil.provideMainViewModelFactory(requireActivity())
    }

    companion object {
        fun newInstance() = LocalFragment()
    }

    override fun processLogic() {
        viewModel.getLocalTrackList(requireContext())

        localTrackAdapter = LocalTrackAdapter({ track: Track -> itemOnClick(track) },
            { track: Track -> trackOptionsOnClick(track) })

        binding.rv.apply {
            adapter = localTrackAdapter
            isSaveEnabled = true
            isSaveFromParentEnabled = true
            scheduleLayoutAnimation()
        }

        viewModel.localTracksLiveData.observe(this, {
            it?.let {
                Log.d(TAG, it.toString())
                localTrackAdapter.submitList(it as MutableList<Track>)
                binding.toolBarLayout.subtitle = "共有${it.size}首歌曲"
                binding.rvPlaylistBottom.visibility = View.VISIBLE
            }
        })

        setupSwipeRefreshLayout()

    }

    private fun setupSwipeRefreshLayout() {
        binding.srl.apply {
            setColorSchemeResources(R.color.blue_900)
            setOnRefreshListener {
                isRefreshing = false
                viewModel.refreshLocalData(requireContext())
                //刷新歌单数据
                mainViewModel.loadAllPlaylist()
                Toast.makeText(requireContext(), "即将刷新列表", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val toolbar = container!!.findViewById<androidx.appcompat.widget.Toolbar>(R.id.tool_bar)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        return binding.root
    }

    private fun trackOptionsOnClick(track: Track) {

        playlistDialog = PlaylistDialog { playlist: Playlist ->
            run {
                mainViewModel.addTrackToPlaylist(track.mediaStoreId, playlist.id)
                toast("已添加到歌单：${playlist.title}")
            }
        }

        val contentView = LayoutInflater.from(requireContext())
            .inflate(
                R.layout.dialog_track_options,
                requireActivity().findViewById(R.id.dialog_track_options),
                false
            )

        val optionTitle =
            contentView.findViewById<MaterialTextView>(R.id.dialog_track_options_tv_title).apply {
                text = track.title
            }

        val optionSize =
            contentView.findViewById<MaterialTextView>(R.id.dialog_track_options_tv_size).apply {
                text = SizeUtils.byteToMbString(track.size)
            }

        val optionPlay = contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_play)

        val optionUpload =
            contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_cloud_upload)

        val optionDownload =
            contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_cloud_download)

        val optionFavorite =
            contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_favorite).apply {
                setOnClickListener {
                    mainViewModel.changeFavoriteMode(track.mediaStoreId)
                }
            }

        val optionAdd = contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_add)

        val optionDelete =
            contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_delete).apply {
                setOnClickListener {
                    mainViewModel.deleteLocalTrack(track)
                }
            }


        val optionCancel =
            contentView.findViewById<MaterialTextView>(R.id.dialog_track_options_btn_cancel)

        val optionFavoriteIcon =
            contentView.findViewById<ShapeableImageView>(R.id.dialog_track_options_favorite_icon)

        val optionFavoriteText =
            contentView.findViewById<MaterialTextView>(R.id.dialog_track_options_favorite_text)

        val favoriteStateObserver = Observer<Boolean> {
            it?.let {

                if (it) {
                    optionFavoriteIcon.setImageLevel(1)
                    optionFavoriteText.text = "取消收藏"
                } else {
                    optionFavoriteIcon.setImageLevel(0)
                    optionFavoriteText.text = "收藏"
                }
            }
        }

        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog).apply {
            setContentView(contentView)
            setOnShowListener {
                mainViewModel.isFavorite(track.mediaStoreId)
                mainViewModel.isTrackFavorite.observeForever(favoriteStateObserver)
            }
            setOnCancelListener {
                mainViewModel.isTrackFavorite.postValue(null)
                mainViewModel.isTrackFavorite.removeObserver(favoriteStateObserver)
            }
            setCanceledOnTouchOutside(true)
            setCancelable(true)
            delegate.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.apply {
                setBackgroundColor(requireContext().resources.getColor(R.color.transparent))
            }
            show()
        }

        optionAdd.setOnClickListener {
            dialog.cancel()
            playlistDialog.show(childFragmentManager, TAG)
            mainViewModel.loadAllPlaylist()
        }

        optionCancel.setOnClickListener {
            dialog.cancel()
        }

        optionUpload.setOnClickListener {
            mainViewModel.postTrackToCloud(track)
            dialog.cancel()
        }

        optionDownload.setOnClickListener {
            //TODO 下载到本地
        }

        optionPlay.setOnClickListener {
            mainViewModel.playTrack(track)
            dialog.cancel()
        }
    }


    private fun itemOnClick(track: Track) {
        mainViewModel.playTrack(track)
    }

    override fun getLayoutId() = R.layout.fragment_local

    override fun getViewModelInstance() = InjectUtil.provideLocalViewModel(requireActivity())

    override fun getViewModelClass() = LocalViewModel::class.java

}

private const val TAG = "LocalFragment"

