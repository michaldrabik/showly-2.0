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

    val ratings = ratingsRepository.movies.loadRatings(items.map { it.movie })
    return items.map { item ->
      item.copy(userRating = ratings.find { item.movie.traktId == it.idTrakt.id }?.rating)
    }
  }
}
