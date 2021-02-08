package com.example.conch.ui.main.local

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.conch.R
import com.example.conch.data.model.Track
import com.example.conch.databinding.FragmentLocalBinding
import com.example.conch.ui.BaseFragment


class LocalFragment : BaseFragment<FragmentLocalBinding, LocalViewModel>() {

    companion object {
        fun newInstance() = LocalFragment()
    }

    override fun processLogic() {

        binding.localTrackCount = ""
        val localTrackAdapter = LocalTrackAdapter{ track -> adapterOnClick(track)
        }
        binding.rv.adapter = localTrackAdapter

        viewModel.localTracksLiveData.observe(this, {
            it?.let {
                localTrackAdapter.submitList(it as MutableList<Track>)
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val toolbar = container!!.findViewById<androidx.appcompat.widget.Toolbar>(R.id.tool_bar)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun adapterOnClick(track: Track) {
        //TODO 跳转至TrackActivity，开始播放
    }

    override fun onStart() {
        super.onStart()
        viewModel.getLocalTrackList(requireContext())
    }

    override fun getLayoutId() = R.layout.fragment_local

    override fun getViewModelInstance() = LocalViewModel(requireActivity().application)

    override fun getViewModelClass() = LocalViewModel::class.java

}