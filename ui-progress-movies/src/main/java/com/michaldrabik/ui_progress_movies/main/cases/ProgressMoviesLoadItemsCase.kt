package com.michaldrabik.ui_progress_movies.main.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_repository.PinnedItemsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.movies.MoviesRepository
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale.ROOT
import javax.inject.Inject

@AppScope
class ProgressMoviesLoadItemsCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val dateFormatProvider: DateFormatProvider
) {

  suspend fun loadWatchlistMovies() = moviesRepository.watchlistMovies.loadAll()

  suspend fun loadProgressItem(movie: Movie, dateFormat: DateTimeFormatter? = null): ProgressMovieItem {
    val isPinned = pinnedItemsRepository.isItemPinned(movie)

    var movieTranslation: Translation? = Translation.EMPTY
    val language = translationsRepository.getLanguage()
    if (language != Config.DEFAULT_LANGUAGE) {
      movieTranslation = translationsRepository.loadTranslation(movie, language, true)
    }

    return ProgressMovieItem(
      movie,
      Image.createUnavailable(ImageType.POSTER),
      isPinned = isPinned,
      movieTranslation = movieTranslation,
      dateFormat = dateFormat
    )
  }

  fun prepareItems(
    input: List<ProgressMovieItem>,
    searchQuery: String,
    sortOrder: SortOrder
  ): List<ProgressMovieItem> {
    return when (sortOrder) {
      NAME -> input.sortedBy {
        val translatedTitle = if (it.movieTranslation?.hasTitle == false) null else it.movieTranslation?.title
        (translatedTitle ?: it.movie.titleNoThe).toUpperCase(ROOT)
      }
      DATE_ADDED -> input.sortedByDescending { it.movie.updatedAt }
      RATING -> input.sortedByDescending { it.movie.rating }
      NEWEST -> input.sortedWith(
        compareByDescending<ProgressMovieItem> { it.movie.released }
          .thenByDescending { it.movie.year }
      )
      else -> throw IllegalStateException("Invalid sort order")
    }
      .filter { !it.movie.hasNoDate() }
      .filter {
        if (searchQuery.isBlank()) true
        else it.movie.title.contains(searchQuery, true) ||
          it.movieTranslation?.title?.contains(searchQuery, true) == true
      }
  }

  fun loadDateFormat() = dateFormatProvider.loadFullDayFormat()
}
