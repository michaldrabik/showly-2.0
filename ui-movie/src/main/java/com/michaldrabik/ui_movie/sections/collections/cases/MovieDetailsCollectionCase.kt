package com.michaldrabik.ui_movie.sections.collections.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MovieCollection
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsCollectionCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteDataSource: TraktRemoteDataSource,
) {

  suspend fun loadMovieCollections(movie: Movie): List<MovieCollection> =
    withContext(dispatchers.IO) {
      try {
        val remoteCollections = remoteDataSource.fetchMovieCollections(movie.traktId)
        return@withContext remoteCollections
          .map {
            MovieCollection(
              id = IdTrakt(it.ids.trakt ?: -1),
              name = it.name,
              description = it.description.ifBlank { it.name },
              itemCount = it.item_count
            )
          }
      } catch (error: Throwable) {
        return@withContext emptyList()
      }
    }
}
