package com.michaldrabik.ui_discover_movies.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.DiscoverSortOrder.HOT
import com.michaldrabik.ui_model.DiscoverSortOrder.NEWEST
import com.michaldrabik.ui_model.DiscoverSortOrder.RATING
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.UserTvdbManager
import com.michaldrabik.ui_repository.movies.MoviesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@AppScope
class DiscoverMoviesCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val tvdbUserManager: UserTvdbManager,
  private val imagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  suspend fun isCacheValid() = moviesRepository.discoverMovies.isCacheValid()

  suspend fun loadCachedMovies(filters: DiscoverFilters) = coroutineScope {
//    val myMoviesIds = async { moviesRepository.myShows.loadAllIds() }
//    val watchlistShowsIds = async { moviesRepository.watchlistShows.loadAllIds() }
//    val archiveShowsIds = async { moviesRepository.archiveShows.loadAllIds() }
    val cachedMovies = async { moviesRepository.discoverMovies.loadAllCached() }
    val language = settingsRepository.getLanguage()

    prepareItems(
      cachedMovies.await(),
      emptyList(),
      emptyList(),
      emptyList(),
      filters,
      language
    )
  }

  suspend fun loadRemoteMovies(filters: DiscoverFilters): List<DiscoverMovieListItem> {
    val showAnticipated = !filters.hideAnticipated
    val genres = filters.genres.toList()

    try {
      tvdbUserManager.checkAuthorization()
    } catch (t: Throwable) {
      // Ignore at this moment
    }

//    val myShowsIds = showsRepository.myShows.loadAllIds()
//    val watchlistShowsIds = showsRepository.watchlistShows.loadAllIds()
//    val archiveShowsIds = showsRepository.archiveShows.loadAllIds()
    val remoteMovies = moviesRepository.discoverMovies.loadAllRemote(showAnticipated, genres)
    val language = settingsRepository.getLanguage()

    moviesRepository.discoverMovies.cacheDiscoverMovies(remoteMovies)
    return prepareItems(remoteMovies, emptyList(), emptyList(), emptyList(), filters, language)
  }

  private suspend fun prepareItems(
    movies: List<Movie>,
    myMoviesIds: List<Long>,
    watchlistMoviesIds: List<Long>,
    archiveMoviesIds: List<Long>,
    filters: DiscoverFilters?,
    language: String
  ) = coroutineScope {
    movies
      .filter { !archiveMoviesIds.contains(it.traktId) }
      .sortedBy(filters?.feedOrder ?: HOT)
      .mapIndexed { index, movie ->
        async {
          val itemType = when (index) {
            in (0..500 step 14) -> FANART_WIDE
            in (5..500 step 14), in (9..500 step 14) -> FANART
            else -> POSTER
          }
          val image = imagesProvider.findCachedImage(movie, itemType)
          val translation = loadTranslation(language, itemType, movie)
          DiscoverMovieListItem(
            movie,
            image,
            isCollected = movie.ids.trakt.id in myMoviesIds,
            isWatchlist = movie.ids.trakt.id in watchlistMoviesIds,
            translation = translation
          )
        }
      }.awaitAll()
  }

  private suspend fun loadTranslation(language: String, itemType: ImageType, movie: Movie) =
    if (language == Config.DEFAULT_LANGUAGE || itemType == POSTER) null
    else translationsRepository.loadTranslation(movie, language, true)

  private fun List<Movie>.sortedBy(order: DiscoverSortOrder) = when (order) {
    HOT -> this
    RATING -> this.sortedWith(compareByDescending<Movie> { it.votes }.thenBy { it.rating })
    NEWEST -> this.sortedWith(compareByDescending<Movie> { it.year }.thenByDescending { it.released })
  }
}
