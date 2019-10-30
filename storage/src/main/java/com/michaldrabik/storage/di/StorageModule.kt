package com.michaldrabik.storage.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.DATABASE_NAME
import dagger.Module
import dagger.Provides

@Module
class StorageModule(private val context: Context) {

  @Provides
  fun provideContext(): Context = context.applicationContext

  @Provides
  @StorageScope
  fun providesDatabase(context: Context): AppDatabase {
    Log.d("DAGGER", "creating db")
    return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
  }
}