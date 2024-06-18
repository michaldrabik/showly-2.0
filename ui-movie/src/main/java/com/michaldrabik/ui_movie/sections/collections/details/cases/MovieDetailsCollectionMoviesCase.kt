package com.michaldrabik.ui_movie.sections.collections.details.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MovieCollectionsRepository
import com.michaldrabik.repository.movies.MyMoviesRepository
import com.michaldrabik.repository.movies.WatchlistMoviesRepository
import com.michaldrabik.repository.settings.SettingsSpoilersRepository
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_model.locale.AppLocale
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsCollectionMoviesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val collectionsRepository: MovieCollectionsRepository,
  private val myMoviesRepository: MyMoviesRepository,
  private val watchlistMoviesRepository: WatchlistMoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsSpoilersRepository: SettingsSpoilersRepository,
  private val imagesProvider: MovieImagesProvider,
) {

  suspend fun loadCollectionMovies(
    collectionId: IdTrakt,
    locale: AppLocale
  ): List<MovieDetailsCollectionItem.MovieItem> = withContext(dispatchers.IO) {
    val movies = collectionsRepository.loadCollectionItems(collectionId)
    movies.mapIndexed { index, movie ->
      async {
        MovieDetailsCollectionItem.MovieItem(
          rank = index + 1,
          movie = movie,
          image = imagesProvider.findCachedImage(movie, POSTER),
          isMyMovie = myMoviesRepository.exists(movie.ids.trakt),
          isWatchlist = watchlistMoviesRepository.exists(movie.ids.trakt),
          translation = loadTranslation(movie, locale),
          spoilers = settingsSpoilersRepository.getAll(),
          isLoading = false
        )
      }
    }.awaitAll()
  }

  private suspend fun loadTranslation(movie: Movie, locale: AppLocale): Translation? {
    if (locale == AppLocale.default()) return null
    return translationsRepository.loadTranslation(
      movie = movie,
      locale = locale,
      onlyLocal = true
    )
  }
}
