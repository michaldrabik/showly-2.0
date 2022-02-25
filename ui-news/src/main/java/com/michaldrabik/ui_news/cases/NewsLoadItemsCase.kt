package com.michaldrabik.ui_news.cases

import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.repository.NewsRepository
import com.michaldrabik.repository.UserRedditManager
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_model.NewsItem.Type
import com.michaldrabik.ui_news.recycler.NewsListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@ViewModelScoped
class NewsLoadItemsCase @Inject constructor(
  private val newsRepository: NewsRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val userManager: UserRedditManager,
) {

  suspend fun preloadItems(types: List<Type>) = coroutineScope {
    val showsNewsAsync = async {
      when {
        types.contains(Type.SHOW) || types.isEmpty() -> newsRepository.getCachedNews(Type.SHOW)
        else -> emptyList()
      }
    }
    val moviesNewsAsync = async {
      when {
        types.contains(Type.MOVIE) || types.isEmpty() -> newsRepository.getCachedNews(Type.MOVIE)
        else -> emptyList()
      }
    }

    val (showsNews, moviesNews) = awaitAll(showsNewsAsync, moviesNewsAsync)
    val dateFormat = dateFormatProvider.loadShortDayFormat()

    prepareListItems(showsNews, moviesNews, dateFormat)
  }

  suspend fun loadItems(
    forceRefresh: Boolean,
    types: List<Type>,
  ) = coroutineScope {
    val token = userManager.checkAuthorization()

    val showsNewsAsync = async {
      when {
        types.contains(Type.SHOW) || types.isEmpty() -> newsRepository.loadShowsNews(token, forceRefresh)
        else -> emptyList()
      }
    }

    val moviesNewsAsync = async {
      when {
        types.contains(Type.MOVIE) || types.isEmpty() -> newsRepository.loadMoviesNews(token, forceRefresh)
        else -> emptyList()
      }
    }

    val (showsNews, moviesNews) = awaitAll(showsNewsAsync, moviesNewsAsync)
    val dateFormat = dateFormatProvider.loadShortDayFormat()

    prepareListItems(showsNews, moviesNews, dateFormat)
  }

  private fun prepareListItems(
    showsNews: List<NewsItem>,
    moviesNews: List<NewsItem>,
    dateFormat: DateTimeFormatter,
  ): List<NewsListItem> {
    val timeThreshold = nowUtc().minusMinutes(5)
    return (showsNews + moviesNews)
      .asSequence()
      .distinctBy { it.url }
      .filter { it.isWebLink && it.datedAt.isBefore(timeThreshold) }
      .sortedByDescending { it.datedAt }
      .groupBy { it.datedAt.toLocalZone().dayOfYear }
      .map { news -> news.value.sortedByDescending { it.score } }
      .flatten()
      .map { NewsListItem(it, dateFormat) }
      .toList()
  }
}
