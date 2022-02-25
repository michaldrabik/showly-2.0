package com.michaldrabik.ui_news

import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_news.recycler.NewsListItem

data class NewsUiState(
  val items: List<NewsListItem> = emptyList(),
  val filters: List<NewsItem.Type> = emptyList(),
  val isLoading: Boolean = false,
)
