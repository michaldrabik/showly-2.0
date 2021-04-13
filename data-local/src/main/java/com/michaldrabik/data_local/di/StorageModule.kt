package com.michaldrabik.data_local.di

import android.content.Context
import androidx.room.Room
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.migrations.DATABASE_NAME
import com.michaldrabik.data_local.database.migrations.Migrations.MIGRATIONS
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
