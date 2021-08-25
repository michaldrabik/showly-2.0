package com.michaldrabik.ui_news

import com.michaldrabik.ui_news.recycler.NewsListItem

data class NewsUiState(
  val items: List<NewsListItem> = emptyList(),
  val isLoading: Boolean = false,
)
