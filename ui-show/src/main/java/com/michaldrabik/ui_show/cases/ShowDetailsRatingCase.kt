package com.michaldrabik.ui_show.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsRatingCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
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

  fun loadRating(episode: Episode) =
    ratingsRepository.shows.loadRating(episode)

  suspend fun loadExternalRatings(show: Show) =
    ratingsRepository.shows.external.loadRatings(show)
}
