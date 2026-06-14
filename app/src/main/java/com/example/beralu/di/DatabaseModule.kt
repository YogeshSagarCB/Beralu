package com.example.beralu.di

import android.content.Context
import androidx.room.Room
import com.example.beralu.data.db.BeraluDatabase
import com.example.beralu.data.db.dao.BeraluDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BeraluDatabase {
        return Room.databaseBuilder(
            context,
            BeraluDatabase::class.java,
            "beralu_db"
        ).build()
    }

    @Provides
    fun provideBeraluDao(database: BeraluDatabase): BeraluDao {
        return database.beraluDao()
    }
}
