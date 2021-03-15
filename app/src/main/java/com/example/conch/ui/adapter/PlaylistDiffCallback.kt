package com.example.conch.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.conch.data.model.Playlist

object PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {

    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.id == newItem.id
    }

}
