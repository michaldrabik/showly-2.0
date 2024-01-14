package com.michaldrabik.showly2.di.module

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WorkModule {

  @Provides
  @Singleton
  fun providesWorkManager(@ApplicationContext context: Context) =
    WorkManager.getInstance(context)
}
