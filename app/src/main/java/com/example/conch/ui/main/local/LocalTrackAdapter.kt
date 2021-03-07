package com.example.conch.ui.main.local

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
import com.example.conch.utils.TrackDiffCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class LocalTrackAdapter(private val onClick: (Track) -> Unit) :
    ListAdapter<Track, LocalTrackAdapter.ViewHolder>(TrackDiffCallback),
    CoroutineScope by MainScope() {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    class ViewHolder(itemView: View, val onClick: (Track) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val trackTitle = itemView.findViewById<TextView>(R.id.item_local_track_title)
        private val trackArtist = itemView.findViewById<TextView>(R.id.item_local_track_artist)
        private val trackCover = itemView.findViewById<ImageView>(R.id.item_local_track_cover)

        fun bind(track: Track) {

            itemView.setOnClickListener {
                onClick(track)
            }

            trackTitle.text = track.title
            trackArtist.text = track.artist


            if (track.coverPath.trim().isNotEmpty()) {

                Glide.with(trackCover)
                    .load(Uri.parse(track.coverPath))
                    .override(120, 120)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(trackCover)
                return
            }


            Glide.with(trackCover)
                .load(R.drawable.ic_music_note)
                .into(trackCover)

        }

        fun bindLastItem(track: Track) {

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_track, parent, false)

        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = getItem(position)
        scope.launch {

            if (track.id == 0L) {
                holder.bindLastItem(track)
                return@launch
            }

            holder.bind(track)

        }
    }

    override fun submitList(list: MutableList<Track>?) {
        super.submitList(list)

        //用于标记列表末尾
        val newItem = Track(id = 0L)

        if (list?.find { it.id == 0L } == null) {
            list!!.add(newItem)
        }
    }
}
