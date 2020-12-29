package com.michaldrabik.ui_progress_movies.main.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_repository.PinnedItemsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.movies.MoviesRepository
import java.util.Locale.ROOT
import javax.inject.Inject

@AppScope
class ProgressMoviesLoadItemsCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  suspend fun loadWatchlistMovies() = moviesRepository.watchlistMovies.loadAll()

  suspend fun loadProgressItem(movie: Movie): ProgressMovieItem {
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
      movieTranslation = movieTranslation
    )
  }

  fun prepareItems(
    input: List<ProgressMovieItem>,
    searchQuery: String,
    sortOrder: SortOrder
  ): List<ProgressMovieItem> {
    return input
      .sortedWith(
        when (sortOrder) {
          NAME -> compareBy {
            val translatedTitle = if (it.movieTranslation?.hasTitle == false) null else it.movieTranslation?.title
            (translatedTitle ?: it.movie.title).toUpperCase(ROOT)
          }
          DATE_ADDED -> compareByDescending { it.movie.updatedAt }
          else -> throw IllegalStateException("Invalid sort order")
        }
      )
      .filter {
        if (searchQuery.isBlank()) true
        else it.movie.title.contains(searchQuery, true) ||
          it.movieTranslation?.title?.contains(searchQuery, true) == true
      }
  }
}
