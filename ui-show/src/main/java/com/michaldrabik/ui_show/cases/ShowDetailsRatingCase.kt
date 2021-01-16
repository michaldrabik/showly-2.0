package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_repository.RatingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import javax.inject.Inject

@AppScope
class ShowDetailsRatingCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository
) {

  suspend fun addRating(show: Show, rating: Int) {
    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.shows.addRating(token, show, rating)
  }

  suspend fun deleteRating(show: Show) {
    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.shows.deleteRating(token, show)
  }

  suspend fun loadRating(show: Show): TraktRating? {
    val token = userTraktManager.checkAuthorization().token
    return ratingsRepository.shows.loadRating(token, show)
  }

  fun loadRating(episode: Episode) = ratingsRepository.shows.loadRating(episode)
}
