package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.TraktRating
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.repository.rating.RatingsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsRatingCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository
) {

  suspend fun addRating(show: Show, rating: Int) {
    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.addRating(token, show, rating)
  }

  suspend fun deleteRating(show: Show) {
    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.deleteRating(token, show)
  }

  suspend fun loadRating(show: Show): TraktRating? {
    val token = userTraktManager.checkAuthorization().token
    return ratingsRepository.loadRating(token, show)
  }
}
