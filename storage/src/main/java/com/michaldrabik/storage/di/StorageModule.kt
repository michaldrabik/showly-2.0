package com.michaldrabik.storage.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.michaldrabik.storage.database.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class StorageModule(private val context: Context) {

  companion object {
    private const val DATABASE_NAME = "SHOWLY2_DB"
    private const val USER_PREFERENCES_KEY = "USER_PREFERENCES"
    private const val IMAGES_PREFERENCES_KEY = "IMAGES_PREFERENCES"
  }

  @Provides
  fun provideContext(): Context = context.applicationContext

  @Provides
  fun providesDatabase(context: Context): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()

  @Provides
  @Named("userPreferences")
  fun providesSharedPreferences(context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(USER_PREFERENCES_KEY, MODE_PRIVATE)

  @Provides
  @Named("imagesPreferences")
  fun providesImagesSharedPreferences(context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(IMAGES_PREFERENCES_KEY, MODE_PRIVATE)
}