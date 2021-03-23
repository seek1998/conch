package com.example.conch.ui.main

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.conch.data.MyResult
import com.example.conch.data.TrackRepository
import com.example.conch.data.UserRepository
import com.example.conch.data.dto.IOProgress
import com.example.conch.data.model.Track
import kotlinx.coroutines.launch
import java.util.*

class RemoteTrackIOViewModel(application: Application) : AndroidViewModel(application) {

    private val trackRepository = TrackRepository.getInstance()

    private val handler = Handler(Looper.getMainLooper())

    private val userId = UserRepository.getInstance().loggedInUser.id

    val uploadQueue: Queue<Track> = LinkedList()

    val downloadQueue: Queue<Track> = LinkedList()

    val succeedTrackUploadResult = MutableLiveData<MyResult<Track>>()

    val loadingTrackUploadResult = MutableLiveData<MyResult<Track>>()

    val errorResult = MutableLiveData<MyResult<Track>>()

    val currentUploadTrack = MutableLiveData<Track>()

    val currentUploadProgress = MutableLiveData<IOProgress>()

    val currentDownloadTrack = MutableLiveData<Track>()

    val currentDownloadProgress = MutableLiveData<IOProgress>()

    private var checkUploadQueue = true.apply {
        checkIOQueue()
    }

    fun addTrackToUploadQueue(track: Track) {
        uploadQueue.offer(track)
    }

    private fun checkIOQueue(): Boolean = handler.postDelayed({
        //currentUploadTrack的值为null的时候，检查上传队列中是否还有track
        currentUploadTrack.value ?: kotlin.run {
            //uploadQueue为空的时候，poll()返回null
            uploadQueue.poll()?.let {
                val track = it
                Log.d(TAG, "start upload: ${track.title}")
                val uploadProgress = IOProgress(track.mediaStoreId, 0L, track.size)
                currentUploadProgress.postValue(uploadProgress)
                currentUploadTrack.postValue(track)
                saveRemoteTrackInfo(track)
                uploadTrackCover(track)
                uploadTrackFile(track)
            }
        }

        currentDownloadTrack.value ?: kotlin.run {
            downloadQueue.poll()?.let {
                val track = it
                Log.i(TAG, "start download: ${track.title}")
                val downloadProgress = IOProgress(track.id, 0L, track.size)
                currentDownloadProgress.postValue(downloadProgress)
                currentDownloadTrack.postValue(track)
                saveRemoteTrackInfo(track)
                downloadTrackFile(track)
                downloadTrackCover(track)
            }
        }

        if (this.checkUploadQueue) {
            checkIOQueue()
        }

    }, 500)


    val uploadProgressObserver = Observer<IOProgress?> {
        it?.let {

            if (it.current == it.total) {
                //上传完成后,使currentUploadTrack置空， 使checkIOQueue可以继续填充
                currentUploadTrack.value?.run {
                    val succeedResult = MyResult.Success(this)
                    succeedTrackUploadResult.postValue(succeedResult)
                }

                handler.postDelayed({
                    currentUploadTrack.postValue(null)
                }, 200)
            }

            val currentTrack = currentUploadTrack.value?.title
            Log.d(TAG, "currentTrack:  $currentTrack, IOProgress: $it")
        }
    }

    private fun uploadTrackCover(track: Track) =
        viewModelScope.launch {
            trackRepository.uploadTrackCover(track, userId)
        }

    private fun uploadTrackFile(track: Track) =
        viewModelScope.launch {
            trackRepository.uploadTrackFile(track, userId, currentUploadProgress)
        }

    @Suppress("ThrowableNotThrown")
    fun getTrackFileFromCloud(track: Track) {
        viewModelScope.launch {
            val isLocal = trackRepository.isRemoteTrackLocal(track)

            if (isLocal) {
                val exception = Exception("本地已经存在该歌曲")
                val errorResult = MyResult.Error(exception)
                this@RemoteTrackIOViewModel.errorResult.postValue(errorResult)
                return@launch
            } else {
                downloadQueue.offer(track)
            }
        }
    }

    fun downloadTrackFile(track: Track) =
        viewModelScope.launch {
            trackRepository.downloadTrackFile(track)
        }

    fun downloadTrackCover(track: Track) =
        viewModelScope.launch {
            trackRepository.downloadTrackCover(track)
        }

    private fun saveRemoteTrackInfo(track: Track) =
        viewModelScope.launch {
            trackRepository.insertTracks(track)
        }

    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RemoteTrackIOViewModel(application) as T
        }
    }
}

private const val TAG = "RemoteTrackIOViewModel"