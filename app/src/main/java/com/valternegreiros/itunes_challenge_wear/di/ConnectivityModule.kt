package com.valternegreiros.itunes_challenge_wear.di

import android.content.Context
import com.valternegreiros.itunes_challenge_wear.data.connectivity.AndroidNetworkConnectivityObserver
import com.valternegreiros.itunes_challenge_wear.data.connectivity.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConnectivityModule {

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(
        @ApplicationContext context: Context
    ): NetworkConnectivityObserver {
        return AndroidNetworkConnectivityObserver(context)
    }
}
