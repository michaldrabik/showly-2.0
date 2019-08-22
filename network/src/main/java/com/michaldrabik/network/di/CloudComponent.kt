package com.michaldrabik.network.di

import com.michaldrabik.network.Cloud
import dagger.Component

@Component(modules = [TraktModule::class])
interface CloudComponent {

  fun provideCloud(): Cloud
}