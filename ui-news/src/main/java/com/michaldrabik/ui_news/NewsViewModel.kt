package com.michaldrabik.ui_news

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_news.cases.NewsLoadItemsCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsViewModel @Inject constructor(
  private val loadNewsCase: NewsLoadItemsCase,
) : BaseViewModel<NewsUiModel>() {

  init {
    loadItems()
  }

  fun loadItems() {
    viewModelScope.launch {
      try {
        uiState = NewsUiModel(isLoading = true)
        val items = loadNewsCase.loadItems()
        uiState = NewsUiModel(items = items, isLoading = false)
      } catch (error: Throwable) {
        uiState = NewsUiModel(isLoading = false)
        rethrowCancellation(error)
      }
    }
  }
}
