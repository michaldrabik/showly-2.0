package com.michaldrabik.data_local.di

import android.content.Context
import androidx.room.Room
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.migrations.DATABASE_NAME
import com.michaldrabik.data_local.database.migrations.Migrations.MIGRATIONS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class StorageModule {

  @Provides
  @Singleton
  fun providesDatabase(@ApplicationContext context: Context): AppDatabase {
    Timber.d("Creating database...")
    return Room.databaseBuilder(
      context.applicationContext,
      AppDatabase::class.java,
      DATABASE_NAME
    ).apply {
      MIGRATIONS.forEach { addMigrations(it) }
    }.build()
  }
}
