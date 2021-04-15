package com.michaldrabik.repository

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_model.NewsItem.Type.MOVIE
import com.michaldrabik.ui_model.NewsItem.Type.SHOW
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AppScope
class NewsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  private var showsNewsCache: List<NewsItem>? = null
  private var showsNewsCacheTimestamp = 0L

  private var moviesNewsCache: List<NewsItem>? = null
  private var moviesNewsCacheTimestamp = 0L

  suspend fun loadShowsNews(token: RedditAuthToken): List<NewsItem> {
    val isCacheValid = nowUtcMillis() - showsNewsCacheTimestamp <= TimeUnit.SECONDS.toMillis(15)
    if (showsNewsCache != null && isCacheValid) {
      return showsNewsCache!!.toList()
    }

    val remoteItems = cloud.redditApi.fetchTelevision(token.token)
      .filterNot { it.is_self }
      .map { mappers.news.fromNetwork(it, SHOW) }

    showsNewsCache = remoteItems
    showsNewsCacheTimestamp = nowUtcMillis()

    return showsNewsCache?.toList() ?: emptyList()
  }

  suspend fun loadMoviesNews(token: RedditAuthToken): List<NewsItem> {
    val isCacheValid = nowUtcMillis() - moviesNewsCacheTimestamp <= TimeUnit.SECONDS.toMillis(15)
    if (moviesNewsCache != null && isCacheValid) {
      return moviesNewsCache!!.toList()
    }

    val remoteItems = cloud.redditApi.fetchMovies(token.token)
      .filterNot { it.is_self }
      .map { mappers.news.fromNetwork(it, MOVIE) }

    moviesNewsCache = remoteItems
    moviesNewsCacheTimestamp = nowUtcMillis()

    return moviesNewsCache?.toList() ?: emptyList()
  }

  fun clear() {
    showsNewsCache = null
    showsNewsCacheTimestamp = 0L
    moviesNewsCache = null
    moviesNewsCacheTimestamp = 0L
  }
}
