package com.michaldrabik.ui_progress_movies.progress.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_progress_movies.helpers.ProgressMoviesItemsSorter
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@Singleton
class ProgressMoviesItemsCase @Inject constructor(
    private val sorter: ProgressMoviesItemsSorter,
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

    val sortOrder = settingsRepository.sortSettings.progressMoviesSortOrder
    val sortType = settingsRepository.sortSettings.progressMoviesSortType

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
          dateFormat = dateFormat,
          sortOrder = sortOrder
        )
      }
    }.awaitAll()

    val filtered = filterItems(searchQuery, items)
    val sorted = filtered.sortedWith(sorter.sort(sortOrder, sortType))
    prepareItems(sorted)
  }

  private fun filterItems(query: String, items: List<ProgressMovieListItem.MovieItem>) =
    items.filter {
      it.movie.title.contains(query, true) ||
        it.translation?.title?.contains(query, true) == true
    }

  private fun prepareItems(items: List<ProgressMovieListItem.MovieItem>) =
    items
      .asSequence()
      .filter { !it.movie.hasNoDate() }
      .filter { it.movie.released == null || it.movie.hasAired() }
      .sortedByDescending { it.isPinned }
      .toList()
}
