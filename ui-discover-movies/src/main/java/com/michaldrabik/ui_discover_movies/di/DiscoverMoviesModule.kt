package com.michaldrabik.ui_discover_movies.di

import android.content.Context
import com.michaldrabik.ui_base.utilities.extensions.isTablet
import com.michaldrabik.ui_discover_movies.helpers.itemtype.ImageTypeProvider
import com.michaldrabik.ui_discover_movies.helpers.itemtype.PhoneImageTypeProvider
import com.michaldrabik.ui_discover_movies.helpers.itemtype.TabletImageTypeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DiscoverMoviesModule {

  @Provides
  internal fun providesItemTypeProvider(@ApplicationContext context: Context): ImageTypeProvider {
    return if (context.isTablet()) {
      TabletImageTypeProvider()
    } else {
      PhoneImageTypeProvider()
    }
  }
}
