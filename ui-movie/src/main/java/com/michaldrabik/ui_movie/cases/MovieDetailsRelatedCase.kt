package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.movies.MoviesRepository
import javax.inject.Inject

@AppScope
class MovieDetailsRelatedCase @Inject constructor(
  private val moviesRepository: MoviesRepository
) {

  suspend fun loadRelatedMovies(movie: Movie): List<Movie> =
    moviesRepository.relatedMovies.loadAll(movie)
      .sortedWith(compareBy({ it.votes }, { it.rating }))
      .reversed()
}
