package com.michaldrabik.ui_settings.di

import com.michaldrabik.ui_settings.SettingsFragment
import dagger.Subcomponent

@Subcomponent
interface UiSettingsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiSettingsComponent
  }

  fun inject(fragment: SettingsFragment)
}
