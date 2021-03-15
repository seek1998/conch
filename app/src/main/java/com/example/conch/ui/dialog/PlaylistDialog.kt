package com.example.conch.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.conch.R
import com.example.conch.data.model.Playlist
import com.example.conch.ui.adapter.PlaylistDialogAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView

class PlaylistDialog(private val onClick: (Playlist) -> Unit, private val data: List<Playlist>) :
    DialogFragment() {

    private lateinit var dialog: BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val context = requireContext()


        val contentView = LayoutInflater.from(requireContext()).inflate(
            R.layout.dialog_playlist,
            requireActivity().findViewById(R.id.dialog_playlist),
            false
        )

        dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
            .apply {
                setContentView(contentView)
                show()
            }

        val playlistLayoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        val playlistDialogAdapter = PlaylistDialogAdapter(onClick).apply {
            submitList(data as MutableList<Playlist>)
        }

        val rvPlaylist = contentView.findViewById<RecyclerView>(R.id.dialog_playlist_rv).apply {
            layoutManager = playlistLayoutManager
            adapter = playlistDialogAdapter
            isSaveEnabled = true
            isSaveFromParentEnabled = true
        }

        val btnCancel =
            contentView.findViewById<MaterialTextView>(R.id.dialog_playlist_tv_cancel).apply {
                setOnClickListener {
                    dialog.cancel()
                }
            }

        return dialog
    }

    fun cancel() {
        dialog.cancel()
    }

}


