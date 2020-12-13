package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_repository.movies.MoviesRepository
import javax.inject.Inject

@AppScope
class MovieDetailsMainCase @Inject constructor(
  private val moviesRepository: MoviesRepository
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    moviesRepository.movieDetails.load(idTrakt)
}
