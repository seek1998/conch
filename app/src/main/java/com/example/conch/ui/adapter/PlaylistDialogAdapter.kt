package com.example.conch.ui.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.data.TrackRepository
import com.example.conch.data.model.Playlist
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class PlaylistDialogAdapter(
    private val onClick: (Playlist) -> Unit
) :
    ListAdapter<Playlist, PlaylistDialogAdapter.ViewHolder>(PlaylistDiffCallback),
    CoroutineScope by MainScope() {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    private val trackRepository = TrackRepository.getInstance()

    class ViewHolder(itemView: View, val onClick: (Playlist) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val eventBus = EventBus.getDefault()

        private val playlistCover =
            itemView.findViewById<ShapeableImageView>(R.id.item_playlist_cover)
        private val playlistTitle =
            itemView.findViewById<MaterialTextView>(R.id.item_playlist_title)

        fun bind(playlist: Playlist, coverPath: String) {

            itemView.setOnClickListener {
                onClick(playlist)
            }

            playlistTitle.text = playlist.title

            if (coverPath.trim().isNotEmpty()) {

                Glide.with(playlistCover)
                    .load(Uri.parse(coverPath))
                    .override(256, 256)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(playlistCover)

                return
            }

            // 没有封面，则加载默认封面
            playlistCover.apply {
                setPadding(32 * 3)
                alpha = 0.25F
            }

            Glide.with(playlistCover)
                .load(R.drawable.ic_conch)
                .into(playlistCover)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)

        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        scope.launch {
            val playlistItem = getItem(position)

            Log.i(TAG, "position = $position, playlist = $playlistItem")

            if (playlistItem.id == 0L) {
                return@launch
            } else {
                val coverPath = trackRepository.getPlaylistCoverPath(playlistItem.id)
                holder.bind(playlistItem, coverPath)
            }

        }
    }
}

private const val TAG = "PlaylistDialogAdapter"

