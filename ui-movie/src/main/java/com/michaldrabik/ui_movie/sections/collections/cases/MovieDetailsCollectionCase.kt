package com.michaldrabik.ui_movie.sections.collections.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.movies.MovieCollectionsRepository
import com.michaldrabik.repository.movies.MovieCollectionsRepository.Source
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MovieCollection
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsCollectionCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val repository: MovieCollectionsRepository,
) {

  suspend fun loadMovieCollections(movie: Movie): Pair<List<MovieCollection>, Source> =
    withContext(dispatchers.IO) {
      try {
        return@withContext repository.loadCollections(movie.ids.trakt)
      } catch (error: Throwable) {
        return@withContext Pair(emptyList(), Source.LOCAL)
      }
    }
}
