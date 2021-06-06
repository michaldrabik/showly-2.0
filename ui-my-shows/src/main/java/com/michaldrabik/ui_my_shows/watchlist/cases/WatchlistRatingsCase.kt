package com.michaldrabik.ui_my_shows.watchlist.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_my_shows.watchlist.recycler.WatchlistListItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WatchlistRatingsCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val userTraktManager: UserTraktManager
) {

  suspend fun loadRatings(items: List<WatchlistListItem>): List<WatchlistListItem> {
    if (!userTraktManager.isAuthorized()) return items

    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.shows.preloadShowsRatings(token)

    return items.map {
      val rating = ratingsRepository.shows.loadRating(token, it.show)
      it.copy(userRating = rating?.rating)
    }
  }
}
