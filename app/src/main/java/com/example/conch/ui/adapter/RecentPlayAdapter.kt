package com.example.conch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.data.model.Track
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob

class RecentPlayAdapter(private val onItemClick: (Track) -> Unit) :
    ListAdapter<Track, RecentPlayAdapter.ViewHolder>(TrackDiffCallback),
    CoroutineScope by MainScope() {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    class ViewHolder(
        itemView: View,
        private val onItemClick: (Track) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val recentPlayCover =
            itemView.findViewById<ShapeableImageView>(R.id.item_recent_play_cover)
        private val recentPlayTitle =
            itemView.findViewById<MaterialTextView>(R.id.item_recent_play_title)

        fun bind(track: Track) {

            itemView.setOnClickListener {
                onItemClick(track)
            }

            val coverPath = track.albumArt

            recentPlayTitle.text = track.title

            if (coverPath.isNotEmpty()) {
                recentPlayCover.apply {
                    Glide.with(this)
                        .load(coverPath)
                        .override(256, 256)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(this)
                }

            } else {
                recentPlayCover.apply {
                    Glide.with(this)
                        .load(R.drawable.ic_play_arrow_blue900)
                        .into(this)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_play, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track)
    }
}

private const val TAG = "RecentPlayAdapter"