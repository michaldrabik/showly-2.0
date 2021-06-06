package com.michaldrabik.ui_movie.cases

import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsMainCase @Inject constructor(
  private val moviesRepository: MoviesRepository
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    moviesRepository.movieDetails.load(idTrakt)
}
