package com.michaldrabik.storage.di

import android.content.Context
import androidx.room.Room
import com.michaldrabik.storage.database.AppDatabase
import dagger.Module
import dagger.Provides

@Module
class StorageModule(private val context: Context) {

  companion object {
    private const val DATABASE_NAME = "SHOWLY2_DATABASE"
  }

  @Provides
  fun provideContext(): Context = context.applicationContext

  @Provides
  fun providesDatabase(context: Context): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
}