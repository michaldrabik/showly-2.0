package com.michaldrabik.showly2.di.module

import android.content.Context
import android.content.SharedPreferences
import com.michaldrabik.common.di.AppScope
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class PreferencesModule(private val context: Context) {

  @Provides
  @AppScope
  @Named("tipsPreferences")
  fun providesTutorialsPreferences(): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_TUTORIALS",
      Context.MODE_PRIVATE
    )

  @Provides
  @AppScope
  @Named("watchlistPreferences")
  fun providesProgressShowsPreferences(): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_WATCHLIST",
      Context.MODE_PRIVATE
    )


  @Provides
  @AppScope
  @Named("progressMoviesPreferences")
  fun providesProgressMoviesPreferences(): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_PROGRESS_MOVIES",
      Context.MODE_PRIVATE
    )

  @Provides
  @AppScope
  @Named("miscPreferences")
  fun providesMiscPreferences(): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_MISC",
      Context.MODE_PRIVATE
    )
}
