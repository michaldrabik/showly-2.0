package com.michaldrabik.showly2.di.module

import com.michaldrabik.showly2.di.component.FragmentComponent
import com.michaldrabik.showly2.di.component.ServiceComponent
import dagger.Module

@Module(subcomponents = [FragmentComponent::class, ServiceComponent::class])
class SubcomponentsModule
