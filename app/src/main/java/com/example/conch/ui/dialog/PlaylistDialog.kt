package com.example.conch.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
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

    private lateinit var playlistAdapter: PlaylistDialogAdapter

    private lateinit var playlistsLiveData: MutableLiveData<MutableList<Playlist>>

    private val playlistsObserver = Observer<MutableList<Playlist>> {
        it?.let {
            Log.d(TAG, "sublist: $it")
            this.playlistAdapter.submitList(it)
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
            }

        val linearLayoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        this.playlistAdapter = PlaylistDialogAdapter(onClick)

        this.playlistsLiveData = mainViewModel.playlists.apply {
            observeForever(playlistsObserver)
        }

        contentView.findViewById<RecyclerView>(R.id.dialog_playlist_rv).apply {
            layoutManager = linearLayoutManager
            adapter = playlistAdapter
        }

        contentView.findViewById<MaterialTextView>(R.id.dialog_playlist_tv_cancel).apply {
            setOnClickListener {
                cancel()
            }
        }

        return dialog
    }


    override fun onStop() {
        Log.d(TAG, "stop")
        super.onStop()
        this.playlistsLiveData.removeObserver(playlistsObserver)
    }

    fun show(manager: FragmentManager) {
        Log.d(TAG, "show")
        this.show(manager, TAG)
    }

    fun cancel() {
        Log.d(TAG, "cancel")
        this.dialog.cancel()
    }
}

private const val TAG = "PlaylistDialog"


