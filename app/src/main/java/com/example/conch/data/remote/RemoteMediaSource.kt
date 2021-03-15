package com.example.conch.data.remote

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RemoteMediaSource private constructor(private val oss: OSSClient) :
    CoroutineScope by MainScope() {

    private val bucketName = "conch-music"

    //异步上传本地音乐至OSS
    fun uploadTrackFile(objectKey: String, uploadUri: Uri, uploadProcess: MutableLiveData<Int>) {
        //PutObjectRequest put = new PutObjectRequest("<bucketName>", "<objectKey>", "<uploadFilePath>");
        val put = PutObjectRequest(
            bucketName,
            objectKey,
            uploadUri
        ).apply {
            progressCallback =
                OSSProgressCallback { request, currentSize, totalSize ->

                    currentSize.div(totalSize)
                    val progress = (currentSize.div(totalSize.toFloat()) * 1E3).toInt()

                    uploadProcess.postValue(progress)
                    Log.d(
                        "PutObjectFile",
                        "key: $objectKey  当前大小: $currentSize 总大小: $totalSize   进度: $progress "
                    )
                }
        }

        val task: OSSAsyncTask<*> = oss.asyncPutObject(
            put, object : OSSCompletedCallback<PutObjectRequest?, PutObjectResult> {
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

        // task.cancel(); // 可以取消任务。
        // task.waitUntilFinished(); // 等待上传完成。

    }

    fun uploadTrackCover(objectKey: String, coverUri: Uri) {
        val put = PutObjectRequest(
            bucketName,
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

    private fun filesToMultipartBody(files: List<File>): MultipartBody {
        val builder = MultipartBody.Builder()
        for (file in files) {
            // TODO: 16-4-2  这里为了简单起见，没有判断file的类型
            val requestBody: RequestBody = file.asRequestBody("image/png".toMediaTypeOrNull())
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

private const val TAG = "RemoteMediaSource"