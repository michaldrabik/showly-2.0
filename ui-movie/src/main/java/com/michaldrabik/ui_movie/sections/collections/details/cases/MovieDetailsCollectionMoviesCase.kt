package com.michaldrabik.ui_movie.sections.collections.details.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MovieCollectionsRepository
import com.michaldrabik.repository.movies.MyMoviesRepository
import com.michaldrabik.repository.movies.WatchlistMoviesRepository
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
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
  private val imagesProvider: MovieImagesProvider,
) {

  suspend fun loadCollectionMovies(
    collectionId: IdTrakt,
    language: String
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
          translation = loadTranslation(movie, language),
          isLoading = false
        )
      }
    }.awaitAll()
  }

  private suspend fun loadTranslation(movie: Movie, language: String): Translation? {
    if (language == DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(
      movie = movie,
      language = language,
      onlyLocal = true
    )
  }
}
