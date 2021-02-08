package com.example.conch.data.remote

import android.net.Uri
import com.example.conch.data.Result
import com.example.conch.data.model.RegisterInfoVO
import com.example.conch.data.model.Track
import com.example.conch.data.model.User
import com.example.conch.data.remote.api.UserService

class Network {
    companion object {
        private const val OK = 1

        private val userService: UserService = ServiceCreator.create(UserService::class.java)

        val remoteTrackPath = "http://conch-music.oss-cn-hangzhou.aliyuncs.com/track/"

        val remoteCoverPath = "http://conch-music.oss-cn-hangzhou.aliyuncs.com/image/"

        suspend fun login(email: String, password: String): Result<User> {
            val apiResponse = userService.login(email, password)
            return if (apiResponse.code == OK) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.message))
            }

        }

        suspend fun register(registerInfo: RegisterInfoVO): Result<Nothing> {
            val apiResponse = userService.register(registerInfo)
            return if (apiResponse.code == OK) {
                Result.Success(null)
            } else {
                Result.Error(Exception(apiResponse.message))
            }
        }

        suspend fun getCaptcha(email: String, usage: Int): Result<Nothing> {
            val apiResponse = userService.getCaptcha(email, usage)
            return if (apiResponse.code == OK) {
                Result.Success(null)
            } else {
                Result.Error(Exception(apiResponse.message))
            }
        }

        suspend fun fetchTracksByUID(uid: Long): List<Track> {
            return mutableListOf(
                Track(
                    id = 1,
                    title = "Genshin Main",
                    artist = "hoyo mix",
                    coverPath = remoteCoverPath + 1 + ".jpg",
                    localUri = Uri.parse(remoteTrackPath + 1 + ".mp3")
                ),
                Track(
                    id = 2,
                    title = "Liyue",
                    artist = "hoyo mix",
                    coverPath = remoteCoverPath + 2 + ".jpg",
                    localUri = Uri.parse(remoteTrackPath + 2 + ".mp3")
                ),
                Track(
                    id = 2,
                    title = "Qingce",
                    artist = "hoyo mix",
                    coverPath = remoteCoverPath + 3 + ".jpg",
                    localUri = Uri.parse(remoteTrackPath + 3 + ".mp3")
                )
            )
        }
    }

}