package com.michaldrabik.network2.di

import com.michaldrabik.network2.Cloud
import dagger.Component

@Component(modules = [CloudModule::class])
interface CloudComponent {

  fun provideCloud(): Cloud
}