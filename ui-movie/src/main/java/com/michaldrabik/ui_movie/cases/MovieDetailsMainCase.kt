package com.michaldrabik.ui_movie.cases

import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsMainCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    moviesRepository.movieDetails.load(idTrakt)

  suspend fun removeMalformedMovie(idTrakt: IdTrakt) {
    with(moviesRepository) {
      myMovies.delete(idTrakt)
      watchlistMovies.delete(idTrakt)
      movieDetails.delete(idTrakt)
    }
    Timber.d("Removing malformed movie...")
  }
}
