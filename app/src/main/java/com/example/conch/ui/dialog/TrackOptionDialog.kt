package com.example.conch.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.conch.R
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.ui.main.MainViewModel
import com.example.conch.ui.main.RemoteTrackIOViewModel
import com.example.conch.utils.InjectUtil
import com.example.conch.utils.SizeUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class TrackOptionDialog(
    private val activity: Activity,
    private val track: Track
) : DialogFragment(), CoroutineScope by MainScope() {

    private lateinit var dialog: BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val mainViewModel by activityViewModels<MainViewModel> {
            InjectUtil.provideMainViewModelFactory(activity)
        }

        val remoteTrackIOViewModel by activityViewModels<RemoteTrackIOViewModel> {
            InjectUtil.provideRemoteTrackViewModelFactory(activity)
        }

        val contentView = LayoutInflater.from(requireContext())
            .inflate(
                R.layout.dialog_track_options,
                activity.findViewById(R.id.dialog_track_options),
                false
            )

        contentView.findViewById<MaterialTextView>(R.id.dialog_track_options_tv_title).apply {
            text = track.title
        }

        contentView.findViewById<MaterialTextView>(R.id.dialog_track_options_tv_size).apply {
            text = SizeUtils.byteToMbString(track.size)
        }

        val optionPlay = contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_play)

        val optionUpload =
            contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_cloud_upload)

        val optionDownload =
            contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_cloud_download)

        contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_favorite).apply {
            setOnClickListener {
                mainViewModel.changeFavoriteMode(track.mediaStoreId)
            }
        }

        val optionAdd = contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_add)

        val optionDelete =
            contentView.findViewById<LinearLayout>(R.id.dialog_track_options_btn_delete)

        val optionCancel =
            contentView.findViewById<MaterialTextView>(R.id.dialog_track_options_btn_cancel)

        val optionFavoriteIcon =
            contentView.findViewById<ShapeableImageView>(R.id.dialog_track_options_favorite_icon)

        val optionFavoriteText =
            contentView.findViewById<MaterialTextView>(R.id.dialog_track_options_favorite_text)

        val favoriteStateObserver = Observer<Boolean> {
            it?.let {

                if (it) {
                    optionFavoriteIcon.setImageLevel(1)
                    optionFavoriteText.text = "取消收藏"
                } else {
                    optionFavoriteIcon.setImageLevel(0)
                    optionFavoriteText.text = "收藏"
                }
            }
        }

        this.dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog).apply {

            setContentView(contentView)

            setOnShowListener {
                mainViewModel.isFavorite(track.mediaStoreId)
                mainViewModel.isTrackFavorite.observeForever(favoriteStateObserver)
            }

            setOnCancelListener {
                mainViewModel.isTrackFavorite.postValue(null)
                mainViewModel.isTrackFavorite.removeObserver(favoriteStateObserver)
            }

            setCanceledOnTouchOutside(true)

            setCancelable(true)

            delegate.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.apply {
                setBackgroundColor(ContextCompat.getColor(activity, R.color.transparent))
            }
        }

        optionAdd.setOnClickListener {

            PlaylistDialog { playlist: Playlist ->
                run {
                    mainViewModel.addTrackToPlaylist(track.mediaStoreId, playlist.id)
                    toast("已添加到歌单：${playlist.title}")
                }
            }.show(childFragmentManager)

            mainViewModel.loadAllPlaylist()
        }

        optionCancel.setOnClickListener {
            dialog.cancel()
        }

        optionUpload.setOnClickListener {
            mainViewModel.postTrackToCloud(track)
            dialog.cancel()
        }

        optionDownload.setOnClickListener {
            remoteTrackIOViewModel.getTrackFileFromCloud(track)
        }

        optionPlay.setOnClickListener {
            mainViewModel.playTrack(track)
            dialog.cancel()
        }

        optionDelete.setOnClickListener {
            mainViewModel.deleteLocalTrack(track)
        }

        return this.dialog
    }

    fun show(manager: FragmentManager) {
        this.show(manager, TAG)
    }

    private fun toast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).apply {
            setText(msg)
            show()
        }
    }
}

private const val TAG = "TrackOptionsDialog"