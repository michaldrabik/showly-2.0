package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.movies.MoviesRepository
import javax.inject.Inject

@AppScope
class MovieDetailsCommentsCase @Inject constructor(
  private val moviesRepository: MoviesRepository
) {

  suspend fun loadComments(movie: Movie, limit: Int = 50) =
    moviesRepository.movieDetails.loadComments(movie.ids.trakt, limit)
}
