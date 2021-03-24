package com.example.conch.data.remote

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.GetObjectRequest
import com.alibaba.sdk.android.oss.model.GetObjectResult
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.example.conch.data.dto.IOProgress
import com.example.conch.data.model.Track
import com.example.conch.extension.getMediaFileName
import com.example.conch.service.MessageEvent
import com.example.conch.service.MessageType
import com.example.conch.utils.MessageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream


class RemoteMediaSource private constructor(private val oss: OSSClient) :
    CoroutineScope by MainScope() {

    private val eventBus = EventBus.getDefault()

    //异步上传本地音乐至OSS
    fun uploadTrackFile(
        objectKey: String,
        uploadUri: Uri,
        currentUploadProgress: MutableLiveData<IOProgress>,
        track: Track
    ) {

        val put = PutObjectRequest(
            BUCKET_NAME,
            objectKey,
            uploadUri
        ).apply {
            progressCallback =
                OSSProgressCallback { request, currentSize, totalSize ->

                    val progress = IOProgress(track.id, currentSize, totalSize)
                    currentUploadProgress.postValue(progress)

                    Log.d(
                        "PutObjectFile",
                        "key: $objectKey  当前大小: $currentSize 总大小: $totalSize   进度: ${progress.progress}"
                    )
                }
        }

        val task: OSSAsyncTask<*> = oss.asyncPutObject(
            put, object : OSSCompletedCallback<PutObjectRequest?, PutObjectResult> {
                override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult) {
                    Log.d("PutObject", "UploadSuccess")
                    Log.d("ETag", result.eTag)
                    Log.d("RequestId", result.requestId)
                    sendUploadResultMessage(track)
                }

                override fun onFailure(
                    request: PutObjectRequest?,
                    clientExcepion: ClientException?,
                    serviceException: ServiceException?
                ) {
                    // 请求异常。
                    clientExcepion?.printStackTrace()
                    // 服务异常。
                    serviceException?.let {
                        Log.e("ErrorCode", serviceException.errorCode)
                        Log.e("RequestId", serviceException.requestId)
                        Log.e("HostId", serviceException.hostId)
                        Log.e("RawMessage", serviceException.rawMessage)
                    }

                    sendUploadResultMessage(track, isSucceed = false)

                }
            })

        // task.cancel(); // 可以取消任务。
        // task.waitUntilFinished(); // 等待上传完成。
    }

    fun uploadTrackCover(objectKey: String, coverUri: Uri) {
        val put = PutObjectRequest(
            BUCKET_NAME,
            objectKey,
            coverUri
        ).apply {
            progressCallback =
                OSSProgressCallback { request, currentSize, totalSize ->

                    Log.d(
                        "PutObjectCover",
                        "当前大小: $currentSize 总大小: $totalSize"
                    )
                }
        }

        val task: OSSAsyncTask<*> = oss.asyncPutObject(
            put,
            object : OSSCompletedCallback<PutObjectRequest?, PutObjectResult> {
                override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult) {
                    Log.d("PutObject", "UploadSuccess")
                    Log.d("ETag", result.eTag)
                    Log.d("RequestId", result.requestId)
                }

                override fun onFailure(
                    request: PutObjectRequest?,
                    clientExcepion: ClientException?,
                    serviceException: ServiceException?
                ) {
                    // 请求异常。
                    clientExcepion?.printStackTrace()
                    // 服务异常。
                    serviceException?.let {

                        Log.e("ErrorCode", serviceException.errorCode)
                        Log.e("RequestId", serviceException.requestId)
                        Log.e("HostId", serviceException.hostId)
                        Log.e("RawMessage", serviceException.rawMessage)

                    }
                }
            })

    }

    fun downloadTrackFile(objectKey: String, downloadDir: File, track: Track) {
        //下载文件。
        //objectKey等同于objectName，表示从OSS下载文件时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
        val get = GetObjectRequest(BUCKET_NAME, objectKey)

        oss.asyncGetObject(get, object : OSSCompletedCallback<GetObjectRequest?, GetObjectResult> {
            override fun onSuccess(request: GetObjectRequest?, result: GetObjectResult) {
                //开始读取数据。
                val length = result.contentLength
                val buffer = ByteArray(length.toInt())
                var readCount = 0
                Log.d(TAG, "start download ${track.title}, length= $length")
                while (readCount < length) {
                    try {
                        readCount += result.objectContent.read(
                            buffer,
                            readCount,
                            length.toInt() - readCount
                        )
                    } catch (e: Exception) {
                        Log.d(TAG, e.toString())
                        OSSLog.logInfo(e.toString())
                    }
                }
                //将下载后的文件存放在指定的本地路径。
                try {

                    val fileName = "${track.artist}-${track.getMediaFileName()}"
                    val downloadFilePath = File(downloadDir, fileName).absolutePath
                    Log.d(TAG, "downloadFilePath = $downloadFilePath")
                    val fileOutput = FileOutputStream(downloadFilePath)
                    fileOutput.write(buffer)
                    fileOutput.close()
                    //下载完成、通知mediaStore重新扫描
                    Log.d(TAG, "download finish: $fileName")
                    val content = MessageUtils.MediaScanMessage(downloadDir.path, track.type)
                    val messageEvent = MessageEvent(MessageType.ACTION_UPDATE_MEDIA_STORE)
                    eventBus.postSticky(messageEvent)
                    Log.d(TAG, "message send: $fileName")

                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                    OSSLog.logInfo(e.toString())
                }
            }

            override fun onFailure(
                request: GetObjectRequest?, clientException: ClientException,
                serviceException: ServiceException
            ) {
            }
        })
    }

    fun downloadTrackCover(objectKey: String, downloadDir: File, track: Track) {
        //下载文件。
        //objectKey等同于objectName，表示从OSS下载文件时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
        val get = GetObjectRequest(BUCKET_NAME, objectKey)

        oss.asyncGetObject(get, object : OSSCompletedCallback<GetObjectRequest?, GetObjectResult> {
            override fun onSuccess(request: GetObjectRequest?, result: GetObjectResult) {
                //开始读取数据。
                val length = result.contentLength
                val buffer = ByteArray(length.toInt())
                var readCount = 0
                while (readCount < length) {
                    try {
                        readCount += result.objectContent.read(
                            buffer,
                            readCount,
                            length.toInt() - readCount
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                        OSSLog.logInfo(e.toString())
                    }
                }
                //将下载后的文件存放在指定的本地路径。
                try {
                    val fileName = "${track.artist}-${track.title}.jpg"
                    val downloadFile = File(downloadDir, fileName)
                    val fileOutput = FileOutputStream(downloadFile)
                    fileOutput.write(buffer)
                    fileOutput.close()
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    OSSLog.logInfo(e.toString())
                }
            }

            override fun onFailure(
                request: GetObjectRequest?, clientException: ClientException,
                serviceException: ServiceException
            ) {
            }
        })
    }


    private fun sendUploadResultMessage(track: Track, isSucceed: Boolean = true) {
        val messageEvent = MessageEvent(MessageType.TRACK_DATA)
        if (isSucceed) {
            messageEvent.put("success", track)
        } else {
            messageEvent.put("fail", track)
        }
        eventBus.post(messageEvent)
    }

    private fun filesToMultipartBody(files: List<File>, mime: String): MultipartBody {
        val builder = MultipartBody.Builder()
        for (file in files) {
            val requestBody: RequestBody = file.asRequestBody(mime.toMediaTypeOrNull())
            builder.addFormDataPart("file", file.name, requestBody)
        }
        builder.setType(MultipartBody.FORM)
        return builder.build()
    }

    companion object {

        @Volatile
        private var instance: RemoteMediaSource? = null

        fun init(oss: OSSClient) {
            synchronized(this) {
                instance ?: RemoteMediaSource(oss)
                    .also { instance = it }
            }
        }

        fun getInstance() = instance!!
    }

}

private const val BUCKET_NAME = "conch-music"


private const val TAG = "RemoteMediaSource"