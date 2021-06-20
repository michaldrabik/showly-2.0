package com.michaldrabik.ui_progress_movies.progress.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@Singleton
class ProgressMoviesItemsCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val imagesProvider: MovieImagesProvider,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val dateFormatProvider: DateFormatProvider,
) {

  suspend fun loadItems(searchQuery: String) = withContext(Dispatchers.Default) {
    val language = translationsRepository.getLanguage()
    val dateFormat = dateFormatProvider.loadFullDayFormat()

    val watchlistMovies = moviesRepository.watchlistMovies.loadAll()
    val items = watchlistMovies.map { movie ->
      async {
        var translation: Translation? = null
        if (language != Config.DEFAULT_LANGUAGE) {
          translation = translationsRepository.loadTranslation(movie, language, onlyLocal = true)
        }

        ProgressMovieListItem.MovieItem(
          movie = movie,
          image = imagesProvider.findCachedImage(movie, ImageType.POSTER),
          isLoading = false,
          isPinned = pinnedItemsRepository.isItemPinned(movie),
          translation = translation,
          dateFormat = dateFormat
        )
      }
    }.awaitAll()

    val filtered = filterItems(searchQuery, items)
    val sorted = sortItems(filtered)
    prepareItems(sorted)
  }

  private fun filterItems(query: String, items: List<ProgressMovieListItem.MovieItem>) =
    items.filter {
      it.movie.title.contains(query, true) ||
        it.translation?.title?.contains(query, true) == true
    }

  private suspend fun sortItems(items: List<ProgressMovieListItem.MovieItem>): List<ProgressMovieListItem.MovieItem> {
    val sortOrder = settingsRepository.load().progressMoviesSortBy
    return when (sortOrder) {
      SortOrder.NAME -> items.sortedBy {
        val translatedTitle = if (it.translation?.hasTitle == false) null else it.translation?.title
        (translatedTitle ?: it.movie.titleNoThe).uppercase(Locale.ROOT)
      }
      SortOrder.DATE_ADDED -> items.sortedByDescending { it.movie.updatedAt }
      SortOrder.RATING -> items.sortedByDescending { it.movie.rating }
      SortOrder.NEWEST -> items.sortedWith(
        compareByDescending<ProgressMovieListItem.MovieItem> { it.movie.released }.thenByDescending { it.movie.year }
      )
      else -> throw IllegalStateException("Invalid sort order")
    }
  }

  private fun prepareItems(items: List<ProgressMovieListItem.MovieItem>) =
    items
      .asSequence()
      .filter { !it.movie.hasNoDate() }
      .filter { it.movie.released == null || it.movie.hasAired() }
      .sortedByDescending { it.isPinned }
      .toList()
}
