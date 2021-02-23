package com.example.conch.ui.main.local

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.conch.R
import com.example.conch.data.model.Track
import com.example.conch.databinding.FragmentLocalBinding
import com.example.conch.ui.BaseFragment
import com.example.conch.ui.track.TrackActivity
import com.example.conch.utils.InjectUtil


class LocalFragment : BaseFragment<FragmentLocalBinding, LocalViewModel>() {

    private val TAG = this.javaClass.simpleName

    companion object {
        fun newInstance() = LocalFragment()
    }

    override fun processLogic() {


        val localTrackAdapter = LocalTrackAdapter { track -> itemOnClick(track) }
        binding.rv.apply {
            adapter = localTrackAdapter
            isSaveEnabled = true
            isSaveFromParentEnabled = true
            Log.d(TAG, isStateSaved.toString())
        }

        viewModel.localTracksLiveData.observe(this, {
            it?.let {
                localTrackAdapter.submitList(it as MutableList<Track>)
                binding.toolBarLayout.subtitle = "共有${it.size}首歌曲"
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val toolbar = container!!.findViewById<androidx.appcompat.widget.Toolbar>(R.id.tool_bar)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        viewModel.getLocalTrackList(requireContext())
        return binding.root
    }

    private fun itemOnClick(track: Track) {
        viewModel.playTrack(track)
        startActivity(Intent(requireActivity(), TrackActivity::class.java))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }


    override fun onStart() {
        super.onStart()
    }

    override fun getLayoutId() = R.layout.fragment_local

    override fun getViewModelInstance() = InjectUtil.provideLocalViewModel(requireActivity())

    override fun getViewModelClass() = LocalViewModel::class.java

}