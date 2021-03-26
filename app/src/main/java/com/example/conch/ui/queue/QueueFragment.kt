package com.example.conch.ui.queue

import com.example.conch.R
import com.example.conch.data.model.Track
import com.example.conch.databinding.FragmentQueueBinding
import com.example.conch.ui.BaseFragment
import com.example.conch.ui.adapter.LocalTrackAdapter
import com.example.conch.utils.InjectUtil

class QueueFragment : BaseFragment<FragmentQueueBinding, QueueViewModel>() {

    private lateinit var adapter: LocalTrackAdapter

    override fun onStart() {
        super.onStart()
        viewModel.loadQueue()
    }

    override fun processLogic() {

        setUpRecycleView()

        viewModel.tracks.observe(this, {
            it?.let {

            }
        })
    }

    private fun setUpRecycleView() {

        adapter = LocalTrackAdapter(
            onItemClick = { track -> onItemClick(track) }
        )

        binding.fragmentQueueRecycleView.apply {
            adapter = adapter
        }
    }

    private fun onItemClick(track: Track) {

    }

    override fun getLayoutId() = R.layout.fragment_queue

    override fun getViewModelInstance() = InjectUtil.provideQueueViewModel(requireActivity())

    override fun getViewModelClass() = QueueViewModel::class.java

}