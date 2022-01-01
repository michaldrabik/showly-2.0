package com.michaldrabik.ui_base.common.sheets.ratings.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.TraktRating
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class RatingsMovieCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
) {

  companion object {
    private val RATING_VALID_RANGE = 1..10
  }

  suspend fun loadRating(idTrakt: IdTrakt): TraktRating {
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
    val rating = ratingsRepository.movies.loadRatings(listOf(movie))
    return rating.firstOrNull() ?: TraktRating.EMPTY
  }

  suspend fun saveRating(idTrakt: IdTrakt, rating: Int) {
    check(rating in RATING_VALID_RANGE)

    val token = userTraktManager.checkAuthorization().token
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))

    ratingsRepository.movies.addRating(token, movie, rating)
  }

  suspend fun deleteRating(idTrakt: IdTrakt) {
    val token = userTraktManager.checkAuthorization().token
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))

    ratingsRepository.movies.deleteRating(token, movie)
  }
}
