package com.michaldrabik.network.di

import com.michaldrabik.network.di.module.AwsModule
import com.michaldrabik.network.di.module.OkHttpModule
import com.michaldrabik.network.di.module.RetrofitModule
import com.michaldrabik.network.di.module.TmdbModule
import com.michaldrabik.network.di.module.TraktModule
import dagger.Component

@CloudScope
@Component(
  modules = [
    RetrofitModule::class,
    OkHttpModule::class,
    TraktModule::class,
    TmdbModule::class,
    AwsModule::class
  ]
)
interface CloudComponent : CloudMarker
