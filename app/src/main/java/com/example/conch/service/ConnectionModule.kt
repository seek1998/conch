package com.example.conch.service

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ActivityContext


import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
object ConnectionModule {

    @Singleton
    @Provides
    fun provideConnection(@ActivityContext context: Context): MusicServiceConnection {
        return MusicServiceConnection.getInstance(context)
    }
}