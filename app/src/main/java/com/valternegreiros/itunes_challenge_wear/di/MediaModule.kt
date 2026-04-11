package com.valternegreiros.itunes_challenge_wear.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.datasource.okhttp.OkHttpDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(OkHttpDataSource.Factory(okHttpClient))
            )
            .build()
    }
}
