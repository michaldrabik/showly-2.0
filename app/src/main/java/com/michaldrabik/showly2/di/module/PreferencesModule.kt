package com.michaldrabik.showly2.di.module

import android.content.Context
import android.content.SharedPreferences
import com.michaldrabik.showly2.di.scope.AppScope
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
  fun providesWatchlistPreferences(): SharedPreferences =
    context.applicationContext.getSharedPreferences(
      "PREFERENCES_WATCHLIST",
      Context.MODE_PRIVATE
    )
}
