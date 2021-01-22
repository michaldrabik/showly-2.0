package com.michaldrabik.ui_premium.di

import com.michaldrabik.ui_premium.PremiumFragment
import dagger.Subcomponent

@Subcomponent
interface UiPremiumComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiPremiumComponent
  }

  fun inject(fragment: PremiumFragment)
}
