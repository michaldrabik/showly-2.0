package com.michaldrabik.ui_movie.sections.collections.details.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.mappers.CollectionMapper
import com.michaldrabik.repository.movies.MovieCollectionsRepository
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsCollectionMoviesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val collectionsRepository: MovieCollectionsRepository,
  private val collectionMapper: CollectionMapper,
  private val imagesProvider: MovieImagesProvider,
) {

  suspend fun loadCollectionMovies(collectionId: IdTrakt): List<MovieDetailsCollectionItem.MovieItem> =
    withContext(dispatchers.IO) {
      delay(1500)
      val movies = collectionsRepository.loadCollectionItems(collectionId)
      movies.mapIndexed { index, movie ->
        async {
          MovieDetailsCollectionItem.MovieItem(
            rank = index + 1,
            movie = movie,
            image = imagesProvider.findCachedImage(movie, POSTER),
            isMyMovie = true,
            isWatchlist = false,
            translation = null,
            isLoading = false
          )
        }
      }.awaitAll()
    }
}
