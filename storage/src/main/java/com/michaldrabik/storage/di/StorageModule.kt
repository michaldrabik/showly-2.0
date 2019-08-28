package com.michaldrabik.storage.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Named


@Module
class StorageModule(private val context: Context) {

  companion object {
    private const val USER_PREFERENCES_KEY = "USER_PREFERENCES"
    private const val IMAGES_PREFERENCES_KEY = "IMAGES_PREFERENCES"
  }

  @Provides
  fun provideContext(): Context = context.applicationContext

  @Provides
  @Named("userPreferences")
  fun providesSharedPreferences(context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(USER_PREFERENCES_KEY, MODE_PRIVATE)

  @Provides
  @Named("imagesPreferences")
  fun providesImagesSharedPreferences(context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(IMAGES_PREFERENCES_KEY, MODE_PRIVATE)
}