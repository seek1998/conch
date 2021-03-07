package com.example.conch.ui.main.account

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.conch.R
import com.example.conch.data.model.Track
import com.example.conch.utils.TrackDiffCallback
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob

class RecentPlayAdapter(private val onClick: (Track) -> Unit, private val context: Context) :
    ListAdapter<Track, RecentPlayAdapter.ViewHolder>(TrackDiffCallback),
    CoroutineScope by MainScope() {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    class ViewHolder(
        itemView: View,
        private val onClick: (Track) -> Unit,
        private val context: Context
    ) : RecyclerView.ViewHolder(itemView) {

        private val recentPlayCover =
            itemView.findViewById<ShapeableImageView>(R.id.item_recent_play_cover)


        fun bind(track: Track) {

            itemView.setOnClickListener {
                onClick(track)
            }

            val coverPath = track.coverPath

            if (coverPath.trim().isNotEmpty()) {

                Glide.with(context)
                    .load(Uri.parse(coverPath))
                    .override(256, 256)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(recentPlayCover)

                return
            }

            // 没有封面，则加载默认封面
            recentPlayCover.setBackgroundColor(context.getColor(R.color.divider))
            recentPlayCover.alpha = 0.5F
            Glide.with(context)
                .load(R.drawable.ic_music_note)
                .into(recentPlayCover)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_play, parent, false)
        return ViewHolder(view, onClick, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, position.toString())
        val track = getItem(position)
        holder.bind(track)
    }

    override fun submitList(list: MutableList<Track>?) {
        super.submitList(list)
        Log.d(TAG, list.toString())
    }
}

private const val TAG = "RecentPlayAdapter"