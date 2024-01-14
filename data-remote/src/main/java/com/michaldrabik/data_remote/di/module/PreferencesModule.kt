package com.michaldrabik.data_remote.di.module

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

  @Provides
  @Singleton
  @Named("networkPreferences")
  fun providesNetworkPreferences(@ApplicationContext context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_NETWORK",
      Context.MODE_PRIVATE
    )
}
