package com.example.conch.ui.main.account

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
import com.example.conch.utils.PlaylistDiffCallback
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PlaylistAdapter(
    private val onClick: (Playlist) -> Unit,
    private val createNewPlaylist: () -> Unit
) :
    ListAdapter<Playlist, PlaylistAdapter.ViewHolder>(PlaylistDiffCallback),
    CoroutineScope by MainScope() {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    private val trackRepository = TrackRepository.getInstance()


    class ViewHolder(
        itemView: View,
        val onClick: (Playlist) -> Unit,
        val createNewPlaylist: () -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {

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
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(playlistCover)

                return
            }

            // 没有封面，则加载默认封面
            playlistCover.setPadding(16 * 3)
            Glide.with(playlistCover)
                .load(R.drawable.ic_conch)
                .into(playlistCover)
        }

        fun bindLastItem(playlist: Playlist) {
            //对末尾的特殊歌单做特殊处理

            itemView.setOnClickListener {
                createNewPlaylist()
            }

            playlistTitle.text = playlist.title
            playlistCover.setPadding(16 * 3)

            Glide.with(playlistCover)
                .load(R.drawable.ic_add)
                .override(256, 256)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(playlistCover)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)

        return ViewHolder(view, onClick, createNewPlaylist)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        scope.launch {
            val playlistItem = getItem(position)

            if (playlistItem.id == Playlist.PLAYLIST_FAVORITE_ID) return@launch

            if (playlistItem.id == 0L) {
                holder.bindLastItem(playlistItem)
            } else {
                val coverPath = trackRepository.getPlaylistCoverPath(playlistItem.id)
                holder.bind(playlistItem, coverPath)
            }

        }
    }

    override fun submitList(list: MutableList<Playlist>?) {
        super.submitList(list)
        //从歌单中，去除“我喜欢的音乐”
        Log.i(TAG, list.toString())
        val newItem = Playlist(id = 0L, "新建歌单")

        if (list?.find { it.id == 0L } == null) {
            list!!.add(newItem)
        }

    }
}

private const val TAG = "PlaylistAdapter"