package com.example.conch.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.conch.R
import com.example.conch.data.model.Playlist
import com.example.conch.ui.adapter.PlaylistDialogAdapter
import com.example.conch.ui.main.MainViewModel
import com.example.conch.utils.InjectUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView

class PlaylistDialog(
    private val onClick: (Playlist) -> Unit
) : DialogFragment() {

    private lateinit var dialog: BottomSheetDialog

    private lateinit var adapter: PlaylistDialogAdapter

    private lateinit var playlistsLiveData: MutableLiveData<MutableList<Playlist>>

    private val playlistsObserver = Observer<MutableList<Playlist>> {
        it?.let {
            adapter.submitList(it)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val mainViewModel by activityViewModels<MainViewModel> {
            InjectUtil.provideMainViewModelFactory(requireActivity())
        }

        val contentView = LayoutInflater.from(requireContext()).inflate(
            R.layout.dialog_playlist,
            requireActivity().findViewById(R.id.dialog_playlist),
            false
        )

        this.dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
            .apply {
                setContentView(contentView)
                show()
            }

        val linearLayoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        adapter = PlaylistDialogAdapter(onClick)

        this.playlistsLiveData = mainViewModel.playlists.apply {
            observeForever(playlistsObserver)
        }

        contentView.findViewById<RecyclerView>(R.id.dialog_playlist_rv).apply {
            layoutManager = linearLayoutManager
            adapter = adapter

        }

        contentView.findViewById<MaterialTextView>(R.id.dialog_playlist_tv_cancel).apply {
            setOnClickListener {
                dialog.cancel()
            }
        }

        return dialog
    }

    override fun onStop() {
        super.onStop()
        this.playlistsLiveData.removeObserver(playlistsObserver)
    }

    fun cancel() = this.dialog.cancel()
}


