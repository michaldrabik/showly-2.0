package com.michaldrabik.repository.mappers

import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.data_remote.reddit.model.RedditItem
import com.michaldrabik.ui_model.NewsItem
import javax.inject.Inject

class NewsMapper @Inject constructor() {

  fun fromNetwork(input: RedditItem, type: NewsItem.Type) = NewsItem(
    id = input.id,
    title = input.title,
    type = type,
    createdAt = dateFromMillis(input.created * 1000)
  )
}
