package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject

@AppScope
class MovieDetailsMainCase @Inject constructor(
  private val moviesRepository: MoviesRepository
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    moviesRepository.movieDetails.load(idTrakt)
}
