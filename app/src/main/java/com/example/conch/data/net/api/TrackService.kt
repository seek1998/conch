package com.example.conch.data.net.api

import com.example.conch.data.model.Track
import com.example.conch.data.net.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackService {

    @GET("tracks")
    suspend fun getTracksByUID(
        @Query("uid") uid: Long
    ): ApiResponse<List<Track>>
}