package com.michaldrabik.ui_news

import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_news.recycler.NewsListItem
import com.michaldrabik.ui_news.views.item.NewsItemViewType

data class NewsUiState(
  val items: List<NewsListItem> = emptyList(),
  val filters: List<NewsItem.Type> = emptyList(),
  val viewType: NewsItemViewType = NewsItemViewType.ROW,
  val isLoading: Boolean = false,
)
