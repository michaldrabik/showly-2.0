package com.michaldrabik.ui_news.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.repository.NewsRepository
import com.michaldrabik.repository.UserRedditManager
import com.michaldrabik.ui_model.NewsItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@AppScope
class NewsLoadItemsCase @Inject constructor(
  private val newsRepository: NewsRepository,
  private val userManager: UserRedditManager,
) {

  suspend fun loadItems(): List<NewsItem> = coroutineScope {
    val token = userManager.checkAuthorization()
    val showsNewsAsync = async { newsRepository.loadShowsNews(token) }
    val moviesNewsAsync = async { newsRepository.loadMoviesNews(token) }

    val (showsNews, moviesNews) = awaitAll(showsNewsAsync, moviesNewsAsync)
    (showsNews + moviesNews)
      .sortedByDescending { it.createdAt.toMillis() }
  }
}
