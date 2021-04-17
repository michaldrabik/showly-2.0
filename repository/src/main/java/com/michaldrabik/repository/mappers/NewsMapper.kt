package com.michaldrabik.repository.mappers

import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_remote.reddit.model.RedditItem
import com.michaldrabik.ui_model.NewsItem
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.News as NewsDb

class NewsMapper @Inject constructor() {

  fun fromNetwork(input: RedditItem, type: NewsItem.Type) = NewsItem(
    id = input.id,
    title = input.title,
    url = input.url,
    type = type,
    score = input.score,
    image = input.findImageUrl()?.replace("&amp;", "&"),
    datedAt = dateFromMillis(input.created_utc * 1000),
    createdAt = nowUtc(),
    updatedAt = nowUtc(),
  )

  fun fromDatabase(input: NewsDb) = NewsItem(
    id = input.idNews,
    title = input.title,
    url = input.url,
    type = NewsItem.Type.fromSlug(input.type),
    score = input.score,
    image = input.image,
    datedAt = dateFromMillis(input.datedAt),
    createdAt = dateFromMillis(input.createdAt),
    updatedAt = dateFromMillis(input.updatedAt),
  )

  fun toDatabase(input: NewsItem) = NewsDb(
    id = 0,
    idNews = input.id,
    title = input.title,
    url = input.url,
    type = input.type.slug,
    image = input.image,
    score = input.score,
    datedAt = input.datedAt.toMillis(),
    createdAt = nowUtcMillis(),
    updatedAt = nowUtcMillis()
  )
}
