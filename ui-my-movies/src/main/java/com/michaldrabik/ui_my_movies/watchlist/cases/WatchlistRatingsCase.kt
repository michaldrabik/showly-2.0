package com.michaldrabik.ui_my_movies.watchlist.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_my_movies.watchlist.recycler.WatchlistListItem
import com.michaldrabik.ui_repository.RatingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import javax.inject.Inject

@AppScope
class WatchlistRatingsCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val userTraktManager: UserTraktManager
) {

  suspend fun loadRatings(items: List<WatchlistListItem>): List<WatchlistListItem> {
    if (!userTraktManager.isAuthorized()) return items

    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.movies.preloadMoviesRatings(token)

    return items.map {
      val rating = ratingsRepository.movies.loadRating(token, it.movie)
      it.copy(userRating = rating?.rating)
    }
  }
}
