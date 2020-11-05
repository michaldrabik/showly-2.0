package com.michaldrabik.network.di.module

import com.michaldrabik.network.aws.api.AwsApi
import com.michaldrabik.network.aws.api.AwsService
import com.michaldrabik.network.di.CloudScope
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
