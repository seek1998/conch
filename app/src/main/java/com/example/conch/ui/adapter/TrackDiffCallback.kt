package com.example.conch.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.conch.data.model.Track


object TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
    }

}