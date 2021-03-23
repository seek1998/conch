package com.example.conch.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.conch.R
import com.example.conch.data.TrackRepository
import com.example.conch.data.model.Track
import com.example.conch.extension.getMediaExt
import com.example.conch.service.MessageEvent
import com.example.conch.service.MessageType
import com.example.conch.utils.SizeUtils
import com.google.android.material.internal.BaselineLayout
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class CloudTrackAdapter(
    private val onClick: (Track) -> Unit
) :
    ListAdapter<Track, CloudTrackAdapter.ViewHolder>(TrackDiffCallback),
    CoroutineScope by MainScope() {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    private val trackRepository = TrackRepository.getInstance()

    class ViewHolder(
        context: Context,
        itemView: View,
        val onClick: (Track) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private var mediaStoreId = 0L

        private val eventBus = EventBus.getDefault()

        private val itemSerialNumber =
            itemView.findViewById<MaterialTextView>(R.id.item_cloud_track_number)
        private val trackTitle = itemView.findViewById<TextView>(R.id.item_cloud_track_title)
        private val trackArtist = itemView.findViewById<TextView>(R.id.item_cloud_track_artist)
        private val trackFileState =
            itemView.findViewById<ImageView>(R.id.item_cloud_track_done)
        private val trackExt =
            itemView.findViewById<MaterialTextView>(R.id.item_cloud_track_file_ext)
        private val trackSize =
            itemView.findViewById<MaterialTextView>(R.id.item_cloud_track_file_size)
        private val loading =
            itemView.findViewById<ContentLoadingProgressBar>(R.id.item_cloud_track_loading)

        fun bind(
            track: Track,
            isLocal: Boolean = false,
            position: Int,
            isLastItem: Boolean = false
        ) {

            mediaStoreId = track.mediaStoreId

            itemView.setOnClickListener {
                onClick(track)
                if (isLocal) {
                    Log.d(TAG, "OnClicked: $track")
                    val messageEvent =
                        MessageEvent(MessageType.TRACK_DATA).put("track_to_play", track)
                    eventBus.post(messageEvent)
                }
            }

            itemSerialNumber.text = position.toString()
            trackTitle.text = track.title
            trackArtist.text = track.artist
            trackExt.text = track.getMediaExt()
            trackSize.text = SizeUtils.byteToMbString(track.size)

            if (isLocal) {
                trackFileState.visibility = View.VISIBLE
            }

            if (isLastItem) {
                itemView.findViewById<BaselineLayout>(R.id.item_cloud_track_divider).visibility =
                    View.INVISIBLE
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cloud_track, parent, false)

        return ViewHolder(parent.context, view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = getItem(position)

        scope.launch {

            val isLocal = trackRepository.isRemoteTrackLocal(track)

            if (position == itemCount - 1) {
                holder.bind(track, isLocal, position, true)
                return@launch
            }

            holder.bind(track, isLocal, position)
        }
    }

    override fun submitList(list: MutableList<Track>?) {
        super.submitList(list)
    }
}

private const val TAG = "CloudTrackAdapter"