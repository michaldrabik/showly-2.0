package com.michaldrabik.ui_my_movies.hidden.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_my_movies.hidden.recycler.HiddenListItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class HiddenRatingsCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val userTraktManager: UserTraktManager
) {

  suspend fun loadRatings(items: List<HiddenListItem>): List<HiddenListItem> {
    if (!userTraktManager.isAuthorized()) return items

    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.shows.preloadShowsRatings(token)

    return items.map {
      val rating = ratingsRepository.movies.loadRating(token, it.movie)
      it.copy(userRating = rating?.rating)
    }
  }
}
