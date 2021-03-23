package com.example.conch.data.remote

import android.util.Log
import com.example.conch.data.MyResult
import com.example.conch.data.model.RegisterInfoVO
import com.example.conch.data.model.Track
import com.example.conch.data.model.User
import com.example.conch.data.remote.api.TrackService
import com.example.conch.data.remote.api.UserService

private const val STATUS_OK = 1

class Network private constructor() {

    private val userService: UserService = ServiceCreator.create(UserService::class.java)

    private val trackService: TrackService = ServiceCreator.create(TrackService::class.java)

    suspend fun login(email: String, password: String): MyResult<User> {
        val apiResponse = userService.login(email, password)
        return if (apiResponse.code == STATUS_OK) {
            MyResult.Success(apiResponse.data)
        } else {
            MyResult.Error(Exception(apiResponse.message))
        }

    }

    suspend fun register(registerInfo: RegisterInfoVO): MyResult<Nothing> {
        val apiResponse = userService.register(registerInfo)
        return if (apiResponse.code == STATUS_OK) {
            MyResult.Success(null)
        } else {
            MyResult.Error(Exception(apiResponse.message))
        }
    }

    suspend fun getCaptcha(email: String, usage: Int): MyResult<Nothing> {
        val apiResponse = userService.getCaptcha(email, usage)
        return if (apiResponse.code == STATUS_OK) {
            MyResult.Success(null)
        } else {
            MyResult.Error(Exception(apiResponse.message))
        }
    }

    suspend fun postTrack(track: Track): MyResult<Track> {
        val apiResponse = trackService.postTrack(track)
        Log.i(TAG, apiResponse.data.toString())
        return if (apiResponse.code == STATUS_OK) {
            val data = apiResponse.data?.apply {
                albumArt = track.albumArt
                contentUri = track.contentUri
            }
            MyResult.Success(data)
        } else {
            MyResult.Error(Exception(apiResponse.message))
        }
    }

    suspend fun fetchTracksByUID(uid: Long): MyResult<List<Track>> {
        val apiResponse = trackService.getAllTracksByUID(uid)
        return if (apiResponse.code == STATUS_OK) {
            MyResult.Success(data = apiResponse.data)
        } else {
            MyResult.Error(Exception(apiResponse.message))
        }
    }

    companion object {

        @Volatile
        private var instance: Network? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: Network()
                    .also { instance = it }
            }
    }

}

private const val TAG = "Network"