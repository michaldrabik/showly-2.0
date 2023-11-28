package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.gcloud.GCloudRemoteDataSource
import com.michaldrabik.data_remote.gcloud.api.GCloudApi
import com.michaldrabik.data_remote.gcloud.api.GCloudService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GCloudModule {

  @Provides
  @Singleton
  internal fun providesGCloudApi(
    @Named("retrofitGCloud") retrofit: Retrofit
  ): GCloudRemoteDataSource =
    GCloudApi(retrofit.create(GCloudService::class.java))
}
