package com.michaldrabik.ui_base.common.sheets.ratings.cases

import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class RatingsShowCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
) {

  companion object {
    private val RATING_VALID_RANGE = 1..10
  }

  suspend fun loadRating(idTrakt: IdTrakt): TraktRating {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
    return try {
      val rating = ratingsRepository.shows.loadRatings(listOf(show))
      rating.firstOrNull() ?: TraktRating.EMPTY
    } catch (error: Throwable) {
      handleError(error)
      TraktRating.EMPTY
    }
  }

  suspend fun saveRating(idTrakt: IdTrakt, rating: Int) {
    check(rating in RATING_VALID_RANGE)
    userTraktManager.checkAuthorization()

    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
    try {
      ratingsRepository.shows.addRating(show, rating)
    } catch (error: Throwable) {
      handleError(error)
    }
  }

  suspend fun deleteRating(idTrakt: IdTrakt) {
    userTraktManager.checkAuthorization()

    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
    try {
      ratingsRepository.shows.deleteRating(show)
    } catch (error: Throwable) {
      handleError(error)
    }
  }

  private suspend fun handleError(error: Throwable) {
    val showlyError = ErrorHelper.parse(error)
    if (showlyError is ShowlyError.UnauthorizedError) {
      userTraktManager.revokeToken()
    }
    throw error
  }
}
