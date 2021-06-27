package com.michaldrabik.ui_news

import com.michaldrabik.ui_news.recycler.NewsListItem

data class NewsUiModel(
  val items: List<NewsListItem>,
  val isLoading: Boolean,
)
