package com.michaldrabik.storage.di

import android.content.Context
import androidx.room.Room
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.DATABASE_NAME
import com.michaldrabik.storage.database.Migrations.MIGRATIONS
import dagger.Module
import dagger.Provides
import timber.log.Timber

@Suppress("PrivatePropertyName")
@Module
class StorageModule(private val context: Context) {

  @Provides
  fun provideContext(): Context = context.applicationContext

  @Provides
  @StorageScope
  fun providesDatabase(context: Context): AppDatabase {
    Timber.d("Creating database...")
    return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
      .apply { MIGRATIONS.forEach { addMigrations(it) } }
      .build()
  }
}
