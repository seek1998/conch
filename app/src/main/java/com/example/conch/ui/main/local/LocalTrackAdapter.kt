package com.example.conch.ui.main.local

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


class LocalTrackAdapter(private val onClick: (Track) -> Unit) :
    ListAdapter<Track, LocalTrackAdapter.LocalTrackViewHolder>(TrackDiffCallback),
    CoroutineScope by MainScope() {

    private lateinit var context: Context

    class LocalTrackViewHolder(itemView: View, val onClick: (Track) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val remoteCoverPath = "http://conch-music.oss-cn-hangzhou.aliyuncs.com/image/"

        private val trackTitle = itemView.findViewById<TextView>(R.id.item_local_track_title)
        private val trackArtist = itemView.findViewById<TextView>(R.id.item_local_track_artist)
        private val trackCover = itemView.findViewById<ImageView>(R.id.item_local_track_cover)


        private var currentTrack: Track? = null

        init {
            itemView.setOnClickListener {
                currentTrack?.let {
                    onClick(it)
                }
            }

        }

        fun bind(track: Track, context: Context) {
            currentTrack = track

            trackTitle.text = track.title
            trackArtist.text = track.artist

            if (track.coverPath.isNotEmpty()) {

                Glide.with(context)
                    .load(Uri.parse(track.coverPath))
                    .override(120, 120)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(trackCover)
            } else {
                Glide.with(context)
                    .load(Uri.parse(remoteCoverPath + 1 + ".jpg" + "!s"))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(trackCover)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocalTrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_track, parent, false)
        this.context = parent.context

        return LocalTrackViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: LocalTrackViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track, context)
    }


}

object TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
    }

}