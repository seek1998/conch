package com.example.conch.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
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
    private val activity: Activity,
    private val mediaStoreId: Long
) : DialogFragment() {

    private lateinit var dialog: BottomSheetDialog

    private lateinit var playlistAdapter: PlaylistDialogAdapter

    private lateinit var playlistsLiveData: MutableLiveData<MutableList<Playlist>>

    private val playlistsObserver = Observer<MutableList<Playlist>> {
        it?.let {
            this.playlistAdapter.submitList(it)
        }
    }

    private val mainViewModel by activityViewModels<MainViewModel> {
        InjectUtil.provideMainViewModelFactory(activity)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val contentView = LayoutInflater.from(activity).inflate(
            R.layout.dialog_playlist,
            requireActivity().findViewById(R.id.dialog_playlist),
            false
        )

        this.dialog = BottomSheetDialog(activity, R.style.BottomSheetDialog)
            .apply {
                setContentView(contentView)
            }

        val linearLayoutManager = LinearLayoutManager(activity).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        this.playlistAdapter =
            PlaylistDialogAdapter { playlist -> onItemClick(playlist = playlist) }

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

    private fun onItemClick(playlist: Playlist) {
        mainViewModel.addTrackToPlaylist(mediaStoreId, playlist.id)
        toast("已添加到歌单：${playlist.title}")
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

    fun toast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).apply {
            setText(msg)
            show()
        }
    }

}

private const val TAG = "PlaylistDialog"


