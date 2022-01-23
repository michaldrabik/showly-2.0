package com.michaldrabik.showly2.di.module

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
class PreferencesModule {

  @Provides
  @Singleton
  @Named("tipsPreferences")
  fun providesTutorialsPreferences(@ApplicationContext context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_TUTORIALS",
      Context.MODE_PRIVATE
    )

  @Provides
  @Singleton
  @Named("watchlistPreferences")
  fun providesProgressShowsPreferences(@ApplicationContext context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_WATCHLIST",
      Context.MODE_PRIVATE
    )

  @Provides
  @Singleton
  @Named("progressOnHoldPreferences")
  fun providesProgressShowsOnHoldPreferences(@ApplicationContext context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_PROGRESS_SHOWS_ON_HOLD",
      Context.MODE_PRIVATE
    )

  @Provides
  @Singleton
  @Named("progressMoviesPreferences")
  fun providesProgressMoviesPreferences(@ApplicationContext context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_PROGRESS_MOVIES",
      Context.MODE_PRIVATE
    )

  @Provides
  @Singleton
  @Named("miscPreferences")
  fun providesMiscPreferences(@ApplicationContext context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_MISC",
      Context.MODE_PRIVATE
    )
}
