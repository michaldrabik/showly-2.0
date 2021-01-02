package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_repository.RatingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import javax.inject.Inject

@AppScope
class MovieDetailsRatingCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository
) {

  suspend fun addRating(movie: Movie, rating: Int) {
    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.movies.addRating(token, movie, rating)
  }

  suspend fun deleteRating(movie: Movie) {
    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.movies.deleteRating(token, movie)
  }

  suspend fun loadRating(movie: Movie): TraktRating? {
    val token = userTraktManager.checkAuthorization().token
    return ratingsRepository.movies.loadRating(token, movie)
  }
}
