package com.michaldrabik.repository

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_model.NewsItem.Type.MOVIE
import com.michaldrabik.ui_model.NewsItem.Type.SHOW
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) {

  companion object {
    const val VALID_CACHE_MINUTES = 360L
  }

  suspend fun getCachedNews(type: NewsItem.Type) =
    localSource.news.getAllByType(type.slug)
      .map { mappers.news.fromDatabase(it) }

  suspend fun loadShowsNews(forceRefresh: Boolean): List<NewsItem> {
    if (!forceRefresh) {
      val cachedNews = getCachedNews(SHOW)
      val cacheTimestamp = cachedNews.firstOrNull()?.createdAt?.toMillis() ?: 0

      val isCacheValid = nowUtcMillis() - cacheTimestamp <= TimeUnit.MINUTES.toMillis(VALID_CACHE_MINUTES)
      if (isCacheValid && getCachedNews(SHOW).isNotEmpty()) {
        return cachedNews.toList()
      }
    }

    val remoteItems = remoteSource.gcloud.fetchTelevisionItems()
      .map { mappers.news.fromNetwork(it, SHOW) }

    val dbItems = remoteItems.map { mappers.news.toDatabase(it) }
    localSource.news.replaceForType(dbItems, SHOW.slug)

    return remoteItems.toList()
  }

  suspend fun loadMoviesNews(forceRefresh: Boolean): List<NewsItem> {
    if (!forceRefresh) {
      val cachedNews = getCachedNews(MOVIE)
      val cacheTimestamp = cachedNews.firstOrNull()?.createdAt?.toMillis() ?: 0

      val isCacheValid = nowUtcMillis() - cacheTimestamp <= TimeUnit.MINUTES.toMillis(VALID_CACHE_MINUTES)
      if (isCacheValid && getCachedNews(MOVIE).isNotEmpty()) {
        return cachedNews.toList()
      }
    }

    val remoteItems = remoteSource.gcloud.fetchMoviesItems()
      .map { mappers.news.fromNetwork(it, MOVIE) }

    val dbItems = remoteItems.map { mappers.news.toDatabase(it) }
    localSource.news.replaceForType(dbItems, MOVIE.slug)

    return remoteItems.toList()
  }
}
