package com.michaldrabik.ui_news

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_news.cases.NewsLoadItemsCase
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NewsViewModel @Inject constructor(
  private val loadNewsCase: NewsLoadItemsCase,
) : BaseViewModel<NewsUiModel>() {

  private var previousRefresh = 0L

  init {
    loadItems()
  }

  fun loadItems(forceRefresh: Boolean = false) {
    if (forceRefresh && nowUtcMillis() - previousRefresh < TimeUnit.SECONDS.toMillis(15)) {
      uiState = NewsUiModel(isLoading = false)
      return
    }
    viewModelScope.launch {
      val progressJob = launchDelayed(700) {
        uiState = NewsUiModel(isLoading = true)
      }
      try {
        if (!forceRefresh) {
          val cachedItems = loadNewsCase.preloadItems()
          uiState = NewsUiModel(items = cachedItems)
        }

        val items = loadNewsCase.loadItems(forceRefresh)
        uiState = NewsUiModel(items = items, isLoading = false)

        if (forceRefresh) {
          previousRefresh = nowUtcMillis()
        }
      } catch (error: Throwable) {
        uiState = NewsUiModel(isLoading = false)
        rethrowCancellation(error)
      } finally {
        progressJob.cancel()
      }
    }
  }
}
