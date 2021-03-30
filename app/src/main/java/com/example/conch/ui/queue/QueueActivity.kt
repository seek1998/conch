package com.example.conch.ui.queue

import androidx.activity.viewModels
import com.example.conch.R
import com.example.conch.data.model.Track
import com.example.conch.databinding.ActivityQueueBinding
import com.example.conch.ui.BaseActivity
import com.example.conch.ui.adapter.LocalTrackAdapter
import com.example.conch.ui.main.MainViewModel
import com.example.conch.utils.InjectUtil

class QueueActivity : BaseActivity<ActivityQueueBinding, QueueViewModel>() {

    private lateinit var trackAdapter: LocalTrackAdapter

    private val mainViewModel by viewModels<MainViewModel> {
        InjectUtil.provideMainViewModelFactory(this)
    }

    override fun processLogic() {

        setUpRecycleView()

        loadQueue()

        viewModel.tracks.observe(this, {

            it?.let {
                trackAdapter.submitList(it)
            }
        })

        binding.activityQueueToolBar.apply {
            setNavigationOnClickListener {
                this@QueueActivity.finish()
            }
        }
    }

    private fun loadQueue() {

        val tracks =
            intent.getParcelableArrayListExtra("tracks") ?: ArrayList<Track>()

        if (tracks.isEmpty()) {
            viewModel.loadQueue()
        } else {
            viewModel.tracks.postValue(tracks)
        }
    }

    private fun setUpRecycleView() {

        trackAdapter = LocalTrackAdapter(
            onItemClick = { track -> onItemClick(track) }
        )

        binding.activityQueueRecycleView.apply {
            adapter = trackAdapter
            scheduleLayoutAnimation()
        }
    }


    private fun onItemClick(track: Track) {
        mainViewModel.playTrack(track)
    }

    override fun getLayoutId() = R.layout.activity_queue

    override fun getViewModelInstance() = InjectUtil.provideQueueViewModel(this)

    override fun getViewModelClass() = QueueViewModel::class.java
}

private const val TAG = "QueueActivity"