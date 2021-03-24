package com.example.conch.ui.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.data.model.Track
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.internal.BaselineLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class LocalTrackAdapter(
    private val onClick: (Track) -> Unit,
    private val onOptionsClick: (Track) -> Unit
) :
    ListAdapter<Track, LocalTrackAdapter.ViewHolder>(TrackDiffCallback),
    CoroutineScope by MainScope() {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    class ViewHolder(
        context: Context,
        itemView: View,
        val onClick: (Track) -> Unit,
        val onOptionsClick: (Track) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private var mediaStoreId = 0L

        private val trackTitle = itemView.findViewById<TextView>(R.id.item_local_track_title)
        private val trackArtist = itemView.findViewById<TextView>(R.id.item_local_track_artist)
        private val trackCover = itemView.findViewById<ImageView>(R.id.item_local_track_cover)
        private val trackOptions =
            itemView.findViewById<ShapeableImageView>(R.id.item_local_track_options)

        fun bind(track: Track, isLastItem: Boolean = false) {

            mediaStoreId = track.id

            itemView.setOnClickListener {
                onClick(track)
            }

            trackOptions.setOnClickListener {
                onOptionsClick(track)
            }

            trackTitle.text = track.title
            trackArtist.text = track.artist

            if (isLastItem) {
                itemView.findViewById<BaselineLayout>(R.id.item_local_track_divider).visibility =
                    View.INVISIBLE
            }

            if (track.albumArt.trim().isNotEmpty()) {
                Glide.with(trackCover)
                    .load(Uri.parse(track.albumArt))
                    .override(120, 120)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(trackCover)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_track, parent, false).apply {
                tag = ITEM_VIEW_TAG_NOT_PLYING
            }

        return ViewHolder(parent.context, view, onClick, onOptionsClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = getItem(position)
        scope.launch {

            if (position == itemCount - 1) {
                holder.bind(track, true)
                return@launch
            }

            holder.bind(track)
        }
    }

    override fun submitList(list: MutableList<Track>?) {
        super.submitList(list)
    }
}

private const val TAG = "LocalTrackAdapter"

const val ITEM_VIEW_TAG_NOT_PLYING = 0

const val ITEM_VIEW_TAG_PLYING = 1

