package com.michaldrabik.data_local.di

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.MainLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataModule {

  @Binds
  @Singleton
  internal abstract fun providesLocalDataSource(source: MainLocalDataSource): LocalDataSource
}
