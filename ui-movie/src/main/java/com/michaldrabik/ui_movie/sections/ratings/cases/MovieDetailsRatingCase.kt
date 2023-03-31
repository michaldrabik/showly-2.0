package com.michaldrabik.ui_movie.sections.ratings.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsRatingCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun loadRating(movie: Movie) = withContext(Dispatchers.IO) {
    ratingsRepository.movies.loadRatings(listOf(movie)).firstOrNull()
  }

  suspend fun loadExternalRatings(movie: Movie) = withContext(Dispatchers.IO) {
    ratingsRepository.movies.external.loadRatings(movie)
  }
}

