package com.michaldrabik.ui_movie.cases

import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.ratings.RatingsRepository
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.TraktRating
import javax.inject.Inject

class MovieDetailsRatingCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
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

  suspend fun loadRatings(movie: Movie): Ratings {
    val ratings = ratingsRepository.movies.loadRatings(movie)
    return ratings.copy(
      trakt = Ratings.Value(String.format("%.1f", movie.rating), false),
      imdb = Ratings.Value(ratings.imdb?.value, false),
      metascore = Ratings.Value(ratings.metascore?.value, false),
      rottenTomatoes = Ratings.Value(ratings.rottenTomatoes?.value, false)
    )
  }
}
