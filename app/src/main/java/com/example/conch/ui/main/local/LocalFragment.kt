package com.example.conch.ui.main.local

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.conch.R
import com.example.conch.data.model.Track
import com.example.conch.databinding.FragmentLocalBinding
import com.example.conch.ui.BaseFragment
import com.example.conch.ui.main.MainViewModel
import com.example.conch.utils.InjectUtil


class LocalFragment : BaseFragment<FragmentLocalBinding, LocalViewModel>() {

    private lateinit var localTrackAdapter: LocalTrackAdapter

    private val mainViewModel by activityViewModels<MainViewModel> {
        InjectUtil.provideMainViewModelFactory(requireActivity())
    }

    companion object {
        fun newInstance() = LocalFragment()
    }

    override fun processLogic() {
        viewModel.getLocalTrackList(requireContext())
        localTrackAdapter = LocalTrackAdapter { track -> itemOnClick(track) }
        binding.rv.apply {
            adapter = localTrackAdapter
            isSaveEnabled = true
            isSaveFromParentEnabled = true
        }

        viewModel.localTracksLiveData.observe(this, {
            it?.let {
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
                Toast.makeText(requireContext(), "即将刷新本地数据", Toast.LENGTH_SHORT).show()
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

    private fun itemOnClick(track: Track) {
        mainViewModel.playTrack(track)
    }

    override fun getLayoutId() = R.layout.fragment_local

    override fun getViewModelInstance() = InjectUtil.provideLocalViewModel(requireActivity())

    override fun getViewModelClass() = LocalViewModel::class.java

}


private const val TAG = "LocalFragment"
