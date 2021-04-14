package com.michaldrabik.ui_news.di

import com.michaldrabik.ui_news.NewsFragment
import dagger.Subcomponent

@Subcomponent
interface UiNewsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiNewsComponent
  }

  fun inject(fragment: NewsFragment)
}
