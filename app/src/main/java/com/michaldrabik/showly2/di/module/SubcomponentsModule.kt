package com.michaldrabik.showly2.di.module

import com.michaldrabik.showly2.di.component.FragmentComponent
import com.michaldrabik.showly2.di.component.ServiceComponent
import com.michaldrabik.ui_base.di.UiBaseComponent
import com.michaldrabik.ui_settings.di.UiSettingsComponent
import dagger.Module

@Module(
  subcomponents = [
    FragmentComponent::class,
    ServiceComponent::class,
    UiBaseComponent::class,
    UiSettingsComponent::class
  ]
)
class SubcomponentsModule
