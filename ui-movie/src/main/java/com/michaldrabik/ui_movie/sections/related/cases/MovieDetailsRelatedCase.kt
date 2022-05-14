package com.michaldrabik.ui_movie.sections.related.cases

import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsRelatedCase @Inject constructor(
  private val moviesRepository: MoviesRepository
) {

  // TODO Add Hidden items
  suspend fun loadRelatedMovies(movie: Movie): List<Movie> =
    moviesRepository.relatedMovies.loadAll(movie)
      .sortedWith(compareBy({ it.votes }, { it.rating }))
      .reversed()
}
