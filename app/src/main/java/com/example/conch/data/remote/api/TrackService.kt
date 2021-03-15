package com.example.conch.data.remote.api

import com.example.conch.data.model.Track
import com.example.conch.data.remote.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackService {

    @GET("tracks")
    fun getTracksByUID(
        @Query("uid") uid: Long
    ): ApiResponse<List<Track>>

}