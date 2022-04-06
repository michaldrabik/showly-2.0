package com.michaldrabik.ui_progress_movies.calendar.cases.items

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_progress_movies.calendar.helpers.filters.CalendarFilter
import com.michaldrabik.ui_progress_movies.calendar.helpers.groupers.CalendarGrouper
import com.michaldrabik.ui_progress_movies.calendar.helpers.sorter.CalendarSorter
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

abstract class CalendarMoviesItemsCase constructor(
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val imagesProvider: MovieImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
) {

  abstract val filter: CalendarFilter
  abstract val grouper: CalendarGrouper
  abstract val sorter: CalendarSorter

  suspend fun loadItems(searchQuery: String? = ""): List<CalendarMovieListItem> =
    withContext(Dispatchers.Default) {
      val now = nowUtc().toLocalZone()
      val language = translationsRepository.getLanguage()
      val dateFormat = dateFormatProvider.loadFullDayFormat()

      val (myMovies, watchlistMovies) = awaitAll(
        async { moviesRepository.myMovies.loadAll() },
        async { moviesRepository.watchlistMovies.loadAll() }
      )

      val elements = (myMovies + watchlistMovies)
        .filter { filter.filter(now, it) }
        .sortedWith(sorter.sort())
        .map { movie ->
          async {
            var translation: Translation? = null
            if (language != Config.DEFAULT_LANGUAGE) {
              translation = translationsRepository.loadTranslation(movie, language, onlyLocal = true)
            }
            CalendarMovieListItem.MovieItem(
              movie = movie,
              image = imagesProvider.findCachedImage(movie, ImageType.POSTER),
              isWatched = myMovies.any { it.traktId == movie.traktId },
              dateFormat = dateFormat,
              translation = translation
            )
          }
        }.awaitAll()

      val queryElements = filterByQuery(searchQuery ?: "", elements)
      grouper.groupByTime(nowUtc(), queryElements)
    }

  private fun filterByQuery(query: String, items: List<CalendarMovieListItem.MovieItem>) =
    items.filter {
      it.movie.title.contains(query, true) ||
        it.translation?.title?.contains(query, true) == true
    }
}
