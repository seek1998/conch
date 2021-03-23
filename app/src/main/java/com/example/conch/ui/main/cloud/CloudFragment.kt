package com.example.conch.ui.main.cloud

import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.conch.R
import com.example.conch.data.MyResult
import com.example.conch.data.model.Track
import com.example.conch.databinding.FragmentCloudBinding
import com.example.conch.ui.BaseFragment
import com.example.conch.ui.adapter.CloudTrackAdapter
import com.example.conch.ui.main.MainViewModel
import com.example.conch.ui.main.RemoteTrackIOViewModel
import com.example.conch.utils.InjectUtil
import com.example.conch.utils.SizeUtils

class CloudFragment : BaseFragment<FragmentCloudBinding, CloudViewModel>() {

    private lateinit var cloudTrackAdapter: CloudTrackAdapter

    private val mainViewModel by activityViewModels<MainViewModel> {
        InjectUtil.provideMainViewModelFactory(requireActivity())
    }

    private val remoteTrackIOViewModel by activityViewModels<RemoteTrackIOViewModel> {
        InjectUtil.provideRemoteTrackViewModelFactory(requireActivity())
    }

    override fun processLogic() {

        viewModel.getDataFromRemote()

        cloudTrackAdapter =
            CloudTrackAdapter { track -> onCloudTrackItemClick(track) }

        binding.rv.apply {
            adapter = cloudTrackAdapter
            isSaveEnabled = true
            isSaveFromParentEnabled = true
            scheduleLayoutAnimation()
        }

        viewModel.fetchRemoteTracksResult.observe(this, {
            it?.let {
                if (it is MyResult.Success) {
                    viewModel.remoteTracks.postValue(it.data)
                    binding.srl.isRefreshing = true

                } else if (it is MyResult.Error) {
                    it.exception.message?.let { message ->
                        toast(message)
                    }
                }
            }
        })

        viewModel.remoteTracks.observe(this, {
            it?.let {
                cloudTrackAdapter.submitList(it as MutableList<Track>)

                binding.toolBarLayout.subtitle = if (it.isEmpty()) {
                    "没有歌曲"
                } else {
                    binding.rvPlaylistBottom.visibility = View.VISIBLE
                    countTotalSize(it)
                }
                binding.srl.isRefreshing = false
            }
        })

        setupSwipeRefreshLayout()
    }

    private fun countTotalSize(list: MutableList<Track>): String {
        var total = 0L
        list.forEach {
            total += it.size
        }
        return SizeUtils.byteToMbString(total)
    }

    private fun setupSwipeRefreshLayout() {
        binding.srl.apply {
            setColorSchemeResources(R.color.light_green_700)
            setOnRefreshListener {
                viewModel.refresh()
            }
        }
    }

    private fun onCloudTrackItemClick(track: Track) {
        remoteTrackIOViewModel.getTrackFileFromCloud(track)
    }

    override fun getLayoutId() = R.layout.fragment_cloud

    override fun getViewModelInstance() = InjectUtil.provideCloudViewModel(requireActivity())

    override fun getViewModelClass() = CloudViewModel::class.java
}

private const val TAG = "CloudFragment"