package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.aws.AwsRemoteDataSource
import com.michaldrabik.data_remote.aws.api.AwsApi
import com.michaldrabik.data_remote.aws.api.AwsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AwsModule {

  @Provides
  @Singleton
  fun providesAwsApi(@Named("retrofitAws") retrofit: Retrofit): AwsRemoteDataSource =
    AwsApi(retrofit.create(AwsService::class.java))
}
