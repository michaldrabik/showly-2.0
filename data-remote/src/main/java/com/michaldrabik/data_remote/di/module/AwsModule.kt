package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.aws.api.AwsApi
import com.michaldrabik.data_remote.aws.api.AwsService
import com.michaldrabik.data_remote.di.CloudScope
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
object AwsModule {

  @Provides
  @CloudScope
  fun providesAwsApi(@Named("retrofitAws") retrofit: Retrofit): AwsApi =
    AwsApi(retrofit.create(AwsService::class.java))
}
