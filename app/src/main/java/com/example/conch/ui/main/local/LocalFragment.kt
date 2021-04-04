package com.example.conch.ui.main.local

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.conch.R
import com.example.conch.data.model.Track
import com.example.conch.databinding.FragmentLocalBinding
import com.example.conch.service.MessageEvent
import com.example.conch.service.MessageType
import com.example.conch.ui.BaseFragment
import com.example.conch.ui.adapter.LocalTrackAdapter
import com.example.conch.ui.dialog.TrackOptionDialog
import com.example.conch.ui.main.MainViewModel
import com.example.conch.utils.InjectUtil
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class LocalFragment : BaseFragment<FragmentLocalBinding, LocalViewModel>() {

    private val mainViewModel by activityViewModels<MainViewModel> {
        InjectUtil.provideMainViewModelFactory(requireActivity())
    }

    private val eventBus: EventBus = EventBus.getDefault()

    override fun processLogic() {

        viewModel.getLocalTracks()

        val localTrackAdapter = LocalTrackAdapter(

            onItemClick = { track -> itemOnClick(track) },

            onOptionsClick = { track ->
                TrackOptionDialog(
                    requireActivity(),
                    track
                ).show(childFragmentManager)
            })

        binding.rv.apply {
            adapter = localTrackAdapter
            isSaveEnabled = true
            isSaveFromParentEnabled = true
            scheduleLayoutAnimation()
        }

        viewModel.localTracks.observe(this, {
            it?.let {

                localTrackAdapter.submitList(it as MutableList<Track>)

                binding.toolBarLayout.subtitle = "共有${it.size}首歌曲"

                binding.rvPlaylistBottom.visibility = View.VISIBLE

                if (binding.srl.isRefreshing) {
                    binding.srl.isRefreshing = false
                    val message = getString(R.string.list_refresh_over)
                    toast(message)
                }
            }
        })

        setupSwipeRefreshLayout()
    }

    private fun setupSwipeRefreshLayout() {

        binding.srl.apply {

            setColorSchemeResources(R.color.blue_900)

            setOnRefreshListener {
                scope.launch {
                    viewModel.refreshLocalData()
                    mainViewModel.loadAllPlaylist()
                }
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
        if (message.type == MessageType.TRACK_DELETE) {
            val mediaStoreId = message.getLong()
            //TODO 控制adapter 删除item
        }
    }

    override fun getLayoutId() = R.layout.fragment_local

    override fun getViewModelInstance() = InjectUtil.provideLocalViewModel(requireActivity())

    override fun getViewModelClass() = LocalViewModel::class.java

}

private const val TAG = "LocalFragment"

