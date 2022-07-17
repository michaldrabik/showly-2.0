package com.michaldrabik.ui_news.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_news.views.item.NewsItemViewType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class NewsViewTypeCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun loadViewType(): NewsItemViewType {
    val viewType = settingsRepository.newsViewType
    return NewsItemViewType.valueOf(viewType)
  }

  fun saveViewType(viewType: NewsItemViewType) {
    settingsRepository.newsViewType = viewType.name
  }
}
