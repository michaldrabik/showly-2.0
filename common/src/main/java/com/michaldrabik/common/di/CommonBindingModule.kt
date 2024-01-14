package com.michaldrabik.common.di

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.common.dispatchers.DefaultCoroutineDispatchers
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CommonBindingModule {

  @Binds
  abstract fun bindCoroutineDispatchers(
    dispatchers: DefaultCoroutineDispatchers
  ): CoroutineDispatchers
}
