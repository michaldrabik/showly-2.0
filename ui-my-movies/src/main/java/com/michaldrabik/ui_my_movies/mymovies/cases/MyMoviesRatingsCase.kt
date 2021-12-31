package com.michaldrabik.ui_my_movies.mymovies.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MyMoviesRatingsCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val userTraktManager: UserTraktManager
) {

  suspend fun loadRatings(items: MutableList<MyMoviesItem>): List<MyMoviesItem> {
    if (!userTraktManager.isAuthorized()) return items

    val ratings = ratingsRepository.movies.loadRatings(items.map { it.movie })
    return items.map { item ->
      item.copy(userRating = ratings.find { item.movie.traktId == it.idTrakt.id }?.rating)
    }
  }
}
