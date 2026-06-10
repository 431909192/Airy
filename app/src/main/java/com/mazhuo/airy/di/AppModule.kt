package com.mazhuo.airy.di

import android.content.Context
import androidx.room.Room
import com.mazhuo.airy.data.local.AppDatabase
import com.mazhuo.airy.data.local.dao.ServerConfigDao
import com.mazhuo.airy.data.local.dao.ServerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase { // 👈 确保是 AppDatabase
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, // 👈 确保是 AppDatabase
            "airy_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideServerDao(database: AppDatabase): ServerDao {
        return database.serverDao()
    }

    @Provides
    @Singleton
    fun provideServerConfigDao(database: AppDatabase): ServerConfigDao {
        return database.serverConfigDao()
    }
}