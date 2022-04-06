package com.michaldrabik.ui_discover_movies.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.DiscoverSortOrder.HOT
import com.michaldrabik.ui_model.DiscoverSortOrder.NEWEST
import com.michaldrabik.ui_model.DiscoverSortOrder.RATING
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.ImageType.PREMIUM
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class DiscoverMoviesCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val imagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  companion object {
    private const val PREMIUM_AD_POSITION = 29
  }

  suspend fun isCacheValid() = moviesRepository.discoverMovies.isCacheValid()

  suspend fun loadCachedMovies(filters: DiscoverFilters) = coroutineScope {
    val myIds = async { moviesRepository.myMovies.loadAllIds() }
    val watchlistIds = async { moviesRepository.watchlistMovies.loadAllIds() }
    val hiddenIds = async { moviesRepository.hiddenMovies.loadAllIds() }
    val cachedMovies = async { moviesRepository.discoverMovies.loadAllCached() }
    val language = translationsRepository.getLanguage()

    prepareItems(
      cachedMovies.await(),
      myIds.await(),
      watchlistIds.await(),
      hiddenIds.await(),
      filters,
      language
    )
  }

  suspend fun loadRemoteMovies(filters: DiscoverFilters) = coroutineScope {
    val showAnticipated = !filters.hideAnticipated
    val showCollection = !filters.hideCollection
    val genres = filters.genres.toList()

    val myAsync = async { moviesRepository.myMovies.loadAllIds() }
    val watchlistSync = async { moviesRepository.watchlistMovies.loadAllIds() }
    val hiddenAsync = async { moviesRepository.hiddenMovies.loadAllIds() }
    val (myIds, watchlistIds, hiddenIds) = awaitAll(myAsync, watchlistSync, hiddenAsync)
    val collectionSize = myIds.size + watchlistIds.size + hiddenIds.size

    val remoteMovies = moviesRepository.discoverMovies.loadAllRemote(showAnticipated, showCollection, collectionSize, genres)
    val language = translationsRepository.getLanguage()

    moviesRepository.discoverMovies.cacheDiscoverMovies(remoteMovies)
    prepareItems(remoteMovies, myIds, watchlistIds, hiddenIds, filters, language)
  }

  private suspend fun prepareItems(
    movies: List<Movie>,
    myMoviesIds: List<Long>,
    watchlistMoviesIds: List<Long>,
    hiddenMoviesIds: List<Long>,
    filters: DiscoverFilters?,
    language: String
  ) = coroutineScope {
    val collectionIds = myMoviesIds + watchlistMoviesIds
    movies
      .filter { !hiddenMoviesIds.contains(it.traktId) }
      .filter {
        if (filters?.hideCollection == false) true
        else !collectionIds.contains(it.traktId)
      }
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
      }
      .awaitAll()
      .toMutableList()
      .apply { insertPremiumAdItem(this) }
      .toList()
  }

  private fun insertPremiumAdItem(items: MutableList<DiscoverMovieListItem>) {
    val isPremium = settingsRepository.isPremium
    val isTimePassed = (nowUtcMillis() - settingsRepository.installTimestamp) > ConfigVariant.PREMIUM_AD_DELAY
    if (isPremium || !isTimePassed) return

    val premiumAd = DiscoverMovieListItem(Movie.EMPTY, Image.createUnknown(PREMIUM))
    if (items.size >= PREMIUM_AD_POSITION) {
      items.add(PREMIUM_AD_POSITION, premiumAd)
    } else if (items.isNotEmpty()) {
      items.add(premiumAd)
    }
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
