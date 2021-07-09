package com.michaldrabik.ui_news.recycler

import com.michaldrabik.ui_model.NewsItem
import java.time.format.DateTimeFormatter

data class NewsListItem(
  val item: NewsItem,
  val dateFormat: DateTimeFormatter,
)
