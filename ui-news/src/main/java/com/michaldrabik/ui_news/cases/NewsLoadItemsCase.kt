package com.michaldrabik.ui_news.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.NewsRepository
import com.michaldrabik.repository.UserRedditManager
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_news.recycler.NewsListItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@AppScope
class NewsLoadItemsCase @Inject constructor(
  private val newsRepository: NewsRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val userManager: UserRedditManager,
) {

  private val newsComparator = compareByDescending<NewsItem> { it.createdAt.dayOfYear }.thenByDescending { it.score }

  suspend fun preloadItems() = coroutineScope {
    val showsNewsAsync = async { newsRepository.getCachedShowsNews() }
    val moviesNewsAsync = async { newsRepository.getCachedMoviesNews() }

    val (showsNews, moviesNews) = awaitAll(showsNewsAsync, moviesNewsAsync)
    val dateFormat = dateFormatProvider.loadShortDayFormat()

    (showsNews + moviesNews)
      .sortedWith(newsComparator)
      .map { NewsListItem(it, dateFormat) }
  }

  suspend fun loadItems() = coroutineScope {
    val token = userManager.checkAuthorization()

    val showsNewsAsync = async { newsRepository.loadShowsNews(token) }
    val moviesNewsAsync = async { newsRepository.loadMoviesNews(token) }

    val (showsNews, moviesNews) = awaitAll(showsNewsAsync, moviesNewsAsync)
    val dateFormat = dateFormatProvider.loadShortDayFormat()

    (showsNews + moviesNews)
      .sortedWith(newsComparator)
      .map { NewsListItem(it, dateFormat) }
  }

}
