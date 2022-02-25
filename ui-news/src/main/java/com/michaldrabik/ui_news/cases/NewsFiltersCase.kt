package com.michaldrabik.ui_news.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.NewsItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class NewsFiltersCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun loadFilters() = settingsRepository.newsFilters

  fun saveFilters(filters: List<NewsItem.Type>) {
    settingsRepository.newsFilters = filters
  }
}
