package com.michaldrabik.ui_my_movies.watchlist.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_my_movies.watchlist.recycler.WatchlistListItem
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
    ratingsRepository.movies.preloadMoviesRatings(token)

    return items.map {
      val rating = ratingsRepository.movies.loadRating(token, it.movie)
      it.copy(userRating = rating?.rating)
    }
  }
}
