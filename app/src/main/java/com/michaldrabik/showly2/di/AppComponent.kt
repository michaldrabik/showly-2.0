package com.michaldrabik.showly2.di

import com.michaldrabik.network2.di.CloudComponent
import com.michaldrabik.showly2.MainActivity
import dagger.Component

@AppScope
@Component(
  dependencies = [CloudComponent::class],
  modules = [AppModule::class]
)
interface AppComponent {
  fun inject(activity: MainActivity)
}

