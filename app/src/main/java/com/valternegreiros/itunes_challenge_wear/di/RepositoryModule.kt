package com.valternegreiros.itunes_challenge_wear.di

import com.valternegreiros.itunes_challenge_wear.data.repository.HomeRepositoryImpl
import com.valternegreiros.itunes_challenge_wear.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository
}
