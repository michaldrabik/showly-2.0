package com.michaldrabik.ui_movie.cases

import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_model.Movie
import javax.inject.Inject

class MovieDetailsRelatedCase @Inject constructor(
  private val moviesRepository: MoviesRepository
) {

  suspend fun loadRelatedMovies(movie: Movie): List<Movie> =
    moviesRepository.relatedMovies.loadAll(movie)
      .sortedWith(compareBy({ it.votes }, { it.rating }))
      .reversed()
}
