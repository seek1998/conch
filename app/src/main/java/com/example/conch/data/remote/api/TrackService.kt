package com.example.conch.data.remote.api

import com.example.conch.data.model.Track
import com.example.conch.data.remote.ApiResponse
import retrofit2.http.*

interface TrackService {

    @GET("tracks/all/{uid}")
    suspend fun getAllTracksByUID(
        @Path("uid") uid: Long
    ): ApiResponse<List<Track>>

    @Headers("Cache-Control:public , max-age=30")
    @POST("tracks")
    suspend fun postTrack(@Body track: Track): ApiResponse<Track>

}