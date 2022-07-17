package com.michaldrabik.ui_news.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_news.views.item.NewsItemViewType
import com.michaldrabik.ui_news.views.item.NewsItemViewType.CARD
import com.michaldrabik.ui_news.views.item.NewsItemViewType.ROW
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

  fun toggleViewType(): NewsItemViewType {
    val newType = when (loadViewType()) {
      ROW -> CARD
      CARD -> ROW
    }
    settingsRepository.newsViewType = newType.name
    return newType
  }
}
