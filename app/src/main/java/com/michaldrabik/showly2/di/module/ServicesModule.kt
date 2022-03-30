package com.michaldrabik.showly2.di.module

import android.content.Context
import android.net.ConnectivityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ServicesModule {

  @Provides
  @Singleton
  fun providesConnectivityManager(@ApplicationContext context: Context) =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}
