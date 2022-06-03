package com.michaldrabik.ui_movie.sections.ratings.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsRatingCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun loadRating(movie: Movie) =
    ratingsRepository.movies.loadRatings(listOf(movie)).firstOrNull()

  suspend fun loadExternalRatings(movie: Movie) =
    ratingsRepository.movies.external.loadRatings(movie)
}
