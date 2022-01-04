package com.michaldrabik.ui_my_shows.myshows.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MyShowsRatingsCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val userTraktManager: UserTraktManager,
) {

  suspend fun loadRatings(items: List<MyShowsItem>): List<MyShowsItem> {
    if (!userTraktManager.isAuthorized()) return items

    val ratings = ratingsRepository.shows.loadRatings(items.map { it.show })
    return items.map { item ->
      item.copy(userRating = ratings.find { item.show.traktId == it.idTrakt.id }?.rating)
    }
  }
}
