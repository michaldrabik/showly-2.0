package com.michaldrabik.ui_my_movies.watchlist.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.TraktRating
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WatchlistRatingsCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val userTraktManager: UserTraktManager,
) {

  suspend fun loadRatings(): Map<IdTrakt, TraktRating?> {
    if (!userTraktManager.isAuthorized()) {
      return emptyMap()
    }
    return ratingsRepository.movies.loadMoviesRatings().associateBy { it.idTrakt }
  }
}
