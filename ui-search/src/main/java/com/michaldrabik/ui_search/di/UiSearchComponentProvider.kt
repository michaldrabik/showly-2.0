package com.michaldrabik.ui_search.di

import com.michaldrabik.ui_statistics.di.UiSearchComponent

interface UiSearchComponentProvider {
  fun provideSearchComponent(): UiSearchComponent
}
