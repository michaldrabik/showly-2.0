package com.michaldrabik.ui_discover.di

import android.content.Context
import com.michaldrabik.ui_base.utilities.extensions.isTablet
import com.michaldrabik.ui_discover.helpers.itemtype.ImageTypeProvider
import com.michaldrabik.ui_discover.helpers.itemtype.PhoneImageTypeProvider
import com.michaldrabik.ui_discover.helpers.itemtype.TabletImageTypeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DiscoverModule {

  @Provides
  internal fun providesItemTypeProvider(@ApplicationContext context: Context): ImageTypeProvider {
    return if (context.isTablet()) {
      TabletImageTypeProvider()
    } else {
      PhoneImageTypeProvider()
    }
  }
}
