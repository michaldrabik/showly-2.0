package com.michaldrabik.ui_news.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.repository.NewsRepository
import com.michaldrabik.ui_model.NewsItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@AppScope
class NewsLoadItemsCase @Inject constructor(
  private val newsRepository: NewsRepository,
) {

  suspend fun loadItems(): List<NewsItem> = coroutineScope {
    val showsNewsAsync = async { newsRepository.loadShowsNews() }
    val moviesNewsAsync = async { newsRepository.loadMoviesNews() }

    val (showsNews, moviesNews) = awaitAll(showsNewsAsync, moviesNewsAsync)
    (showsNews + moviesNews)
      .sortedByDescending { it.createdAt.toMillis() }
  }
}
