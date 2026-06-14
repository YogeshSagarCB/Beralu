package com.example.beralu.di

import com.example.beralu.data.repository.BeraluRepositoryImpl
import com.example.beralu.domain.repository.BeraluRepository
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
    abstract fun bindBeraluRepository(
        beraluRepositoryImpl: BeraluRepositoryImpl
    ): BeraluRepository
}
